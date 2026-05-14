É a estratégia de configuração de resposta em formato de numero.

Campos:

- min: Define o valor numérico mínimo da resposta.
- max: Define o valor numérico máximo da resposta.
- step: Define os "saltos" entre um numero e o próximo, exemplo: 
	- step = 1,  valores possíveis 1, 2, 3, 4
	- step = 4, valores possíveis 1, 4, 8, 12
- allowedDecimal: Configuração que permite números decimais (com virgula)
- allowedNegative: Configuração que permite números negativos (abaixo de zero)
- customErrorMessage: Mensagem de erro customizada.

Exemplo:

    min: 1.0
    max: 120.0
    step: null
    allowedDecimal: false
    allowedNegative: false
    customErrorMessage: null
