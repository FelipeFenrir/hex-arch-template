import { ReactNode } from "react";

export type CrudTemplateSection = {
  id: string;
  title: string;
  hint?: string;
  content: ReactNode;
};

export function ResourceCrudTemplate({
  title,
  sections,
  children,
}: {
  title: string;
  sections: CrudTemplateSection[];
  children?: ReactNode;
}) {
  return (
    <div className="service-view">
      <h2>{title}</h2>
      <div className="crud-sections-grid">
        {sections.map((section) => (
          <section className="crud-section" key={section.id}>
            <div className="crud-section-header">
              <h3>{section.title}</h3>
              {section.hint && <p>{section.hint}</p>}
            </div>
            <div className="crud-section-body">{section.content}</div>
          </section>
        ))}
      </div>
      {children}
    </div>
  );
}

