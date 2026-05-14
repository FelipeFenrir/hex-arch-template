'question' representa uma pergunta que pode ser usada em um questionário ([[questionnaire]]).

Toda question é composta por:

- id: Código Identificador em formato sneak_case (ex: idade_cliente, cor_do_carro, area_servico_pintura).
- label: Label para a a pergunta (ex: Qual a cor do cabelo ?, Qual o seu nome ?, e etc).
- sales_item_reference_code: Código de referencia que caracteriza o item da venda, usado somente quando a negociação é sobre Serviço. (ex: automóvel, cartão, local, e etc).
- active: Identifica o status do registro ([[Status de Parametrização]]).
- [[created_by]]: Dados mínimos do usuário que criou o registro.
- created_at: Data e hora de criação do registro.
- [[update_by]]: Dados mínimos do usuário que atualizou o registro.
- updated_at: Data e hora de atualização do registro.