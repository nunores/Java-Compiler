# CP3

- [x] .class
- [x] .super
- [x] .method
- [x] .limit stack
- [x] .limit locals
  
- [x] Label:
- [x] goto
- [x] Ifs
- [x] Arrays
- [x] Whiles (Loop + if)
- [x] putfield
- [x] getfield
- [x] (x && x)
- [x] (!x)

- [x] Fac.jmm
- [x] HelloWorld.jmm
- [x] MyClass.jmm (ERROR: no main method)
- [x] OperatorPrecedence.jmm
- [x] Simple.jmm
- [x] WhileAndIF.jmm
- [ ] FindMaximum.jmm
    -> "iload -1" => getfield (TO FIX)
- [ ] Lazysort.jmm
    -> "rand.V :=.V ..." => "rand.i32 :=.i32 ..."  
- [ ] Life.jmm -> if (aux143.bo ==.bool 1.bool) goto ifBlock6;
    -> provavelmente a funcao removelast2chars está a ser chamada onde nao deve
- [ ] MonteCarloPi.jmm
    -> "rand1.V" => "rand1.i32"
- [ ] TicTacToe.jmm -> Too many errors...
- [ ] Quicksort.jmm (Did not implemented overload functions)


Erro no ollir (lazysort)
rand.V :=.V invokestatic(MathUtils, "random", 0.i32, 10.i32).V;
(rand = MathUtils.random(0, 10);)