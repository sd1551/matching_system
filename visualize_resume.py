import json
import networkx as nx
import matplotlib.pyplot as plt
from collections import defaultdict
from sentence_transformers import SentenceTransformer
from sklearn.metrics.pairwise import cosine_similarity

class ResumeVisualizer:
    def __init__(self):
        self.graph = nx.Graph()
        self.cluster_colors = ['#FF6B6B', '#4ECDC4', '#45B7D1', '#FFA07A', '#98D8C8']
        self.embedder = SentenceTransformer('paraphrase-multilingual-MiniLM-L12-v2')
        self.theme_mapping = {
            'Опыт работы': 'Профессиональный опыт',
            'Навыки': 'Компетенции',
            'Образование': 'Образование',
            'Проекты': 'Проекты',
            'Цель': 'Карьерные цели'
        }

    def load_data(self, json_path):
        with open(json_path, 'r', encoding='utf-8') as f:
            return json.load(f)

    def analyze_resume(self, data):
        name = data.get('Имя Фамилия', ['Кандидат'])[0]
        self._add_node(name, 'person', 0, name)

        for section, content in data.items():
            theme = self.theme_mapping.get(section, section)
            theme_node = f"Тема: {theme}"
            self._add_node(theme_node, 'theme', 0, theme)
            self.graph.add_edge(name, theme_node, relation='has_theme')

            if section == 'Контактная информация':
                self._process_contacts(content, name, theme_node)
            elif section == 'Опыт работы':
                self._process_experience(content, name, theme_node)
            elif section == 'Навыки':
                self._process_skills(content, name, theme_node)
            elif section == 'Образование':
                self._process_education(content, name, theme_node)
            elif section == 'Проекты':
                self._process_projects(content, name, theme_node)
            elif section == 'Цель':
                self._process_goal(content, name, theme_node)
            else:
                self._process_generic_section(section, content, name, theme_node)

        self._add_semantic_connections()

    def _process_contacts(self, contacts, person_node, theme_node):
        for line in contacts:
            if ':' in line:
                key, value = line.split(':', 1)
                key, value = key.strip().lower(), value.strip()
                if 'телефон' in key:
                    self._add_node(value, 'contact', 1, f"📞 {value}")
                    self.graph.add_edge(person_node, value, relation='contact')
                elif 'email' in key:
                    self._add_node(value, 'contact', 1, f"✉️ {value}")
                    self.graph.add_edge(person_node, value, relation='contact')
                elif 'город' in key:
                    self._add_node(value, 'location', 1, f"📍 {value}")
                    self.graph.add_edge(person_node, value, relation='location')
                self.graph.add_edge(theme_node, value, relation='belongs_to')

    def _process_experience(self, experience, person_node, theme_node):
        current_company = None
        for line in experience:
            if 'должность:' in line.lower():
                position = line.split(':', 1)[1].strip()
                self._add_node(position, 'position', 2, f"💼 {position}")
                self.graph.add_edge(person_node, position, relation='position')
                if current_company:
                    self.graph.add_edge(position, current_company, relation='at')
                    self.graph.add_edge(theme_node, current_company, relation='belongs_to')
            elif any(x in line for x in ['ООО', 'ЗАО', 'ИП']):
                company = line.split(',')[0].strip()
                current_company = company
                self._add_node(company, 'company', 2, f"🏢 {company}")
                self.graph.add_edge(person_node, company, relation='worked_at')
                self.graph.add_edge(theme_node, company, relation='belongs_to')

    def _process_skills(self, skills, person_node, theme_node):
        for line in skills:
            for skill in line.split(','):
                skill = skill.strip()
                if skill and len(skill) > 2:
                    self._add_node(skill, 'skill', 3, f"🛠️ {skill}")
                    self.graph.add_edge(person_node, skill, relation='has_skill')
                    self.graph.add_edge(theme_node, skill, relation='belongs_to')

    def _process_education(self, education, person_node, theme_node):
        for line in education:
            institution = line.split(',')[0].strip()
            self._add_node(institution, 'education', 4, f"🎓 {institution}")
            self.graph.add_edge(person_node, institution, relation='studied_at')
            self.graph.add_edge(theme_node, institution, relation='belongs_to')

    def _process_projects(self, projects, person_node, theme_node):
        for line in projects:
            if line.strip() and (line.startswith('-') or line.startswith('•')):
                project = line[1:].split('(')[0].strip()
                self._add_node(project, 'project', 5, f"🚀 {project}")
                self.graph.add_edge(person_node, project, relation='worked_on')
                self.graph.add_edge(theme_node, project, relation='belongs_to')

    def _process_goal(self, goal, person_node, theme_node):
        goal_text = ' '.join(goal)
        self._add_node(goal_text, 'goal', 6, "🎯 Цель")
        self.graph.add_edge(person_node, goal_text, relation='has_goal')
        self.graph.add_edge(theme_node, goal_text, relation='belongs_to')

    def _process_generic_section(self, section, content, person_node, theme_node):
        section_node = f"Раздел: {section}"
        self._add_node(section_node, 'section', 7, section)
        self.graph.add_edge(person_node, section_node, relation='section')
        self.graph.add_edge(theme_node, section_node, relation='belongs_to')

        texts = [line for line in content if line.strip() and len(line) > 10]
        if texts:
            embeddings = self.embedder.encode(texts)
            sim_matrix = cosine_similarity(embeddings)

            for i, text in enumerate(texts):
                node_id = f"{section}_item_{i}"
                self._add_node(node_id, 'content', 7, text[:50] + '...')
                self.graph.add_edge(section_node, node_id, relation='contains')

                for j, sim in enumerate(sim_matrix[i]):
                    if i != j and sim > 0.7:
                        self.graph.add_edge(node_id, f"{section}_item_{j}",
                                            relation='similar', weight=sim)

    def _add_semantic_connections(self):
        text_nodes = [n for n in self.graph.nodes if isinstance(n, str) and len(n) > 20]
        if text_nodes:
            embeddings = self.embedder.encode(text_nodes)
            sim_matrix = cosine_similarity(embeddings)

            for i, n1 in enumerate(text_nodes):
                for j, n2 in enumerate(text_nodes):
                    if i < j and sim_matrix[i][j] > 0.75:
                        self.graph.add_edge(n1, n2, relation='semantically_related',
                                            weight=sim_matrix[i][j])

    def _add_node(self, node_id, node_type, cluster, label=None):
        self.graph.add_node(node_id,
                            type=node_type,
                            cluster=cluster,
                            label=label or node_id,
                            color=self.cluster_colors[cluster % len(self.cluster_colors)])

    def visualize(self):
        plt.figure(figsize=(20, 20))
    pos = nx.spring_layout(self.graph, k=0.5, seed=42, iterations=50)

    node_colors = [data['color'] for _, data in self.graph.nodes(data=True)]
    node_sizes = [2000 if data['type'] in ['person', 'theme'] else 1200
                  for _, data in self.graph.nodes(data=True)]

    nx.draw_networkx_nodes(self.graph, pos, node_size=node_sizes,
                           node_color=node_colors, alpha=0.9)

    edge_styles = {
        'has_theme': ('black', 'solid'),
        'belongs_to': ('gray', 'dashed'),
        'similar': ('green', 'dotted'),
        'semantically_related': ('blue', 'dotted')
    }

    for rel, (color, style) in edge_styles.items():
        edges = [(u, v) for u, v, d in self.graph.edges(data=True)
                 if d['relation'] == rel]
        nx.draw_networkx_edges(self.graph, pos, edgelist=edges,
                               edge_color=color, style=style, width=2, alpha=0.6)

    other_edges = [(u, v) for u, v, d in self.graph.edges(data=True)
                   if d['relation'] not in edge_styles]
    nx.draw_networkx_edges(self.graph, pos, edgelist=other_edges,
                           edge_color='lightgray', width=1, alpha=0.4)

    nx.draw_networkx_labels(self.graph, pos,
                            labels={n: d['label'] for n, d in self.graph.nodes(data=True)},
                            font_size=9, font_family='Arial')

    plt.title("Визуализация резюме", fontsize=18)
    plt.axis('off')
    plt.tight_layout()

    # Сохраняем изображение перед показом
    plt.savefig('resume_visualization.png', dpi=300, bbox_inches='tight')
    plt.show()

if __name__ == "__main__":
    visualizer = ResumeVisualizer()
    data = visualizer.load_data("resume_data.json")
    visualizer.analyze_resume(data)
    visualizer.visualize()