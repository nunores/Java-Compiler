# CP2

## Semantic Analysis

Todas as verificações feitas na análise semantica pedidas devem reportar erro. Outro tipo de verificações (extra) devem reportar warnings e não erro (ou seja, devem permitir continuar a compilação):

## Symbol Table

- [x] global: inclui info de imports e a classe declarada
- [x] class-specific: inclui info de extends, fields e methods
- [x] method-specific: inclui info dos arguments e local variables
- [x] retorno do SemanticsReport (consulta da tabela por parte da análise semântica e impressão para debug)
- [ ] small bonus: permitir method overload (i.e. métodos com mesmo nome mas assinatura de parâmetros diferente)

## Expression Analysis

### Type Verification

- [x] verificar que as variáveis estão a ser inicializadas
- [x] verificar se operações são efetuadas com o mesmo tipo (e.g. int + boolean tem de dar erro)
- [x] não é possível utilizar arrays diretamente para operações aritmeticas (e.g. array1 + array2)
- [x] verificar se um array access é de facto feito sobre um array (e.g. 1[10] não é permitido)
- [x] verificar se o indice do array access é um inteiro (e.g. a[true] não é permitido)
- [x] verificar se valor do assignee é igual ao do assigned (a_int = b_boolean não é permitido!)
- [x] verificar se operação booleana (&&, < ou !) é efetuada só com booleanos
- [x] verificar se conditional expressions (if e while) resulta num booleano

### Method Verification

- [x] verificar se o "target" do método existe, e se este contém o método (e.g. a.foo, ver se 'a' existe e se tem um método 'foo')
(caso seja do tipo da classe declarada (e.g. a usar o this), se não existir declaração na própria classe: se não tiver extends retorna erro, se tiver extends assumir que é da classe super.)
- [x] caso o método não seja da classe declarada, isto é uma classe importada, assumir como existente e assumir tipos esperados. (e.g. a = Foo.b(), se a é um inteiro, e Foo é uma classe importada, assumir que o método b é estático (pois estamos a aceder a uma método diretamente da classe), que não tem argumentos e que retorna um inteiro)
- [x] verificar se o número de argumentos na invocação é igual ao número de parâmetros da declaração
- [x] verificar se o tipo dos parâmetros coincide com o tipo dos argumentos
- [x] verificar se retorno coincide

## OLLIR Generation

- [x] ClassDeclaration
- [x] MethodDeclaration
- [x] MainDeclaration
- [x] ReturnExpression
- [x] Assignment
- [x] MethodCall
- [x] Operation
- [x] NewInstance
- [x] NewArray
- [x] Expression
- [x] DotLength / ArrayLength
- [x] ArrayAccess
- [x] Not
- [x] Less e And
- [x] Tratar de $ quando é parâmetro

- [ ] Arranjar os \t e \n
- [x] AuxN nos ArrayAccess
- [x] Fazer while
- [x] Not em ifs e whiles

- [ ] .field

## Jasmin Generation

- [x] estrutura básica de classe (incluindo construtor init)
- [ ] estrutura básica de fields
- [x] estrutura básica de métodos (podem desconsiderar os limites neste checkpoint: limit_stack 99, limit_locals 99)
- [x] assignments
- [x] operações aritméticas (com prioridade de operações correta) (neste checkpoint não é necessário a seleção das operações mais eficientes mas isto será considerado no CP3 e versão final)
- [ ] invocação de métodos