import AST.*;
import AST.expressions.*;
import AST.literals.*;
import AST.statements.*;
import AST.types.Basic;
import AST.types.CompoundArray;
import AST.types.CompoundMap;
import AST.types.CompoundTuple;
import norswap.autumn.Grammar;
import norswap.autumn.actions.ActionContext;

public final class GrammarParser extends Grammar {

    // White spaces
    public rule space_char = cpred(Character::isWhitespace);
    public rule comment = seq(
            "#",
            seq(str("\n").not(), any).at_least(0),
            str("\n").opt()
    );

    {
        ws = choice(space_char, comment).at_least(0);
        id_part = cpred(i -> Character.isAlphabetic(i) || i == '_');
    }

    public rule identifier = identifier(id_part.at_least(1))
            .push(ActionContext::str);


    // ===========================================================================================================
    // Keywords
    // ===========================================================================================================

    public rule _false = reserved("false").as_val(false);
    public rule _true = reserved("true").as_val(true);
    public rule _void = reserved("void");
    public rule _var = reserved("var");
    public rule _integer = reserved("integer");
    public rule _bool = reserved("bool");
    public rule _string = reserved("string");
    public rule _array = reserved("array");
    public rule _map = reserved("map");
    public rule _tuple = reserved("tuple");
    public rule _if = reserved("if");
    public rule _elif = reserved("elif");
    public rule _else = reserved("else");
    public rule _while = reserved("while");
    public rule _for = reserved("for");
    public rule _func = reserved("func");
    public rule _ret = reserved("ret");
    public rule _dollar = str("$");
    public rule _at = word("@").as_val("@");

    // ===========================================================================================================
    // Operators / tokens
    // ===========================================================================================================

    public rule AND = word("&&");
    public rule OR = word("||");
    public rule NOT = word("!");

    public rule LPAREN = word("(");
    public rule RPAREN = word(")");
    public rule LBRACKET = word("[");
    public rule RBRACKET = word("]");
    public rule LBRACE = word("{");
    public rule RBRACE = word("}");
    public rule EQ = word("=");
    public rule EQEQ = word("==");
    public rule BANGEQ = word("!=");
    public rule GTEQ = word(">=");
    public rule LTEQ = word("<=");
    public rule GT = word(">");
    public rule LT = word("<");
    public rule COMMA = word(",");
    public rule COLON = word(":");
    public rule TIMES = word("*");
    public rule DIV = word("/");
    public rule MOD = word("%");
    public rule PLUS = word("+");
    public rule MINUS = word("-");

    // Lazy references
    public rule _expr = lazy(() -> this.expression);

    // ===========================================================================================================
    // Literals
    // ===========================================================================================================

    public rule integer_literal = choice('0', seq(character('-').opt(), digit.at_least(1)))
            .push($ -> new IntegerLiteral(Long.parseLong($.str())))
            .word();

    public rule pos_integer_literal = digit.at_least(1)
            .push($ -> Integer.parseInt($.str()))
            .word();

    public rule string_char = choice(
            seq(set('"', '\\', '{', '}').not(), range('\u0000', '\u001F').not(), any),
            seq('\\', set("\\/bfnrt")),
            seq(str("\\u"), hex_digit, hex_digit, hex_digit, hex_digit)
    ).push($ -> $).word();

    public rule string_part = string_char.at_least(1)
            .push($ -> new StringChunk($.str()));

    public rule string_interpolation = seq(LBRACE, _expr, RBRACE)
            .push($ -> new StringInterpolation($.$0()));

    public rule string_literal = seq('"', choice(string_interpolation, string_part).at_least(0), '"')
            .push($ -> new StringLiteral($.$list()));

    public rule boolean_literal = choice(_true, _false).push($ -> new BooleanLiteral($.$0()));

    public rule argv_literal = seq(_dollar, _expr)
            .push($ -> new ArgvLiteral($.$0()));

    public rule argc_literal = seq(_dollar, _at)
            .push($ -> new ArgcLiteral($.$0()));

    public rule arg_literal = choice(argv_literal, argc_literal);

    public rule literal = choice(
            boolean_literal,
            integer_literal,
            string_literal,
            arg_literal
    ).word();

    // ===========================================================================================================
    // Types
    // ===========================================================================================================

    public rule _type = lazy(() -> this.type);

    public rule basic_type = choice(
            _void,
            _var,
            _integer,
            _bool,
            _string
    ).push($ -> new Basic($.str()));

    public rule array_type = seq(_array, LT, _type, GT)
            .push($ -> new CompoundArray($.$0()));

    public rule map_type = seq(_map, LT, _type, COMMA, _type, GT)
            .push($ -> new CompoundMap($.$0(), $.$1()));

    public rule tuple_type = seq(_tuple, LT, _type, GT)
            .push($ -> new CompoundTuple($.$0()));

    public rule type = choice(
            basic_type,
            array_type,
            map_type,
            tuple_type
    );

    // ===========================================================================================================
    // Expressions
    // ===========================================================================================================

    public rule reference = identifier
        .push($ -> new Reference($.$0()));

    public rule par_expr = seq(LPAREN, _expr, RPAREN)
            .push($ -> new Parenthesis($.$0()));

    public rule array = seq(LBRACKET, _expr.sep(0, COMMA), RBRACKET)
            .push($ -> new ArrayLiteral($.$list()));

    public rule tuple = seq(LPAREN, _expr.sep(1, COMMA), RPAREN)
            .push($ -> new TupleLiteral($.$list()));

    public rule pair = seq(_expr, COLON, _expr)
            .push($ -> new PairLiteral($.$0(), $.$1()));

    public rule map = seq(LBRACE, pair.sep(0, COMMA), RBRACE)
            .push($ -> new MapLiteral($.$list()));

    public rule arg = seq(seq(identifier, COLON).or_push_null(), _expr)
            .push($ -> new FunctionParamCall($.$0(), $.$1()));

    public rule args = arg.sep(0, COMMA)
            .push(ActionContext::$list);

    public rule assignable_expr = choice(
            par_expr,
            reference,
            array,
            map,
            tuple,
            literal // string, int, args, boolean
    );

    // ===========================================================================================================
    // Expressions affixes
    // ===========================================================================================================

    public rule postfix_expr = left_expression()
            .left(assignable_expr)
            .suffix(seq(LBRACKET, _expr, RBRACKET), $ -> new DataAccess($.$0(), $.$1()))
            .suffix(seq(LPAREN, args, RPAREN), $ -> new FunctionCall($.$0(), $.$1()));

    public rule prefix = choice(
            NOT.as_val(PrefixOperator.NOT)
    );

    public rule prefix_expr = right_expression()
            .right(postfix_expr)
            .prefix(prefix, $ -> new Prefix($.$0(), $.$1()));

    // ===========================================================================================================
    // Expressions operations
    // ===========================================================================================================

    public rule mul_div_mod_op = choice(
            TIMES.as_val(ArithmeticOperator.TIMES),
            DIV.as_val(ArithmeticOperator.DIV),
            MOD.as_val(ArithmeticOperator.MOD)
    );

    public rule add_sub_op = choice(
            PLUS.as_val(ArithmeticOperator.PLUS),
            MINUS.as_val(ArithmeticOperator.MINUS)
    );

    public rule comparison_op = choice(
            BANGEQ.as_val(ArithmeticOperator.BANGEQ),
            EQEQ.as_val(ArithmeticOperator.EQEQ),
            GTEQ.as_val(ArithmeticOperator.GTEQ),
            LTEQ.as_val(ArithmeticOperator.LTEQ),
            GT.as_val(ArithmeticOperator.GT),
            LT.as_val(ArithmeticOperator.LT)
    );

    // ===========================================================================================================
    // Expressions arithmetic
    // ===========================================================================================================

    public rule mul_expr = left_expression()
            .operand(prefix_expr)
            .infix(mul_div_mod_op, $ -> new Arithmetic($.$0(), $.$1(), $.$2()));

    public rule add_expr = left_expression()
            .operand(mul_expr)
            .infix(add_sub_op, $ -> new Arithmetic($.$0(), $.$1(), $.$2()));

    public rule equality_expr = left_expression()
            .operand(add_expr)
            .infix(comparison_op, $ -> new Arithmetic($.$0(), $.$1(), $.$2()));

    public rule and_expr = left_expression()
            .operand(equality_expr)
            .infix(AND.as_val(ArithmeticOperator.AND), $ -> new Arithmetic($.$0(), $.$1(), $.$2()));

    public rule or_expr = left_expression()
            .operand(and_expr)
            .infix(OR.as_val(ArithmeticOperator.OR), $ -> new Arithmetic($.$0(), $.$1(), $.$2()));

    public rule assignment = right_expression()
            .operand(or_expr)
            .infix(EQ, $ -> new Assignment($.$0(), $.$1()));

    public rule expression = seq(assignment);

    public rule expression_statement = expression
            .filter($ -> {
                if (!($.$[0] instanceof Assignment || $.$[0] instanceof FunctionCall))
                    return false;
                $.push(new ExpressionStatement($.$0()));
                return true;
            });
    // ===========================================================================================================
    // Statements
    // ===========================================================================================================

    public rule _statement = lazy(() -> this.statement);
    public rule _block = lazy(() -> this.block);

    public rule var_definition =
            seq(type, identifier, seq(EQ, expression).or_push_null())
                    .push($ -> new VarDeclaration($.$0(), $.$1(), $.$2()));

    public rule elif_statement = seq(_elif, par_expr, _block)
            .push($ -> new Elif($.$0(), $.$1()));

    public rule elif_statement_list = elif_statement.at_least(0)
            .push(ActionContext::$list);

    public rule if_statement =
            seq(_if, par_expr, _block, elif_statement_list, seq(_else, _block).or_push_null())
                    .push($ -> new If($.$0(), $.$1(), $.$2(), $.$3()));

    public rule while_statement = seq(_while, par_expr, _block)
            .push($ -> new While($.$0(), $.$1()));

    public rule for_in_cursor = seq(identifier, COLON, expression)
            .push($ -> new ForInCursor($.$0(), $.$1()));

    public rule for_in_statement = seq(_for, LPAREN, for_in_cursor, RPAREN, _block)
            .push($ -> new ForIn($.$0(), $.$1()));

    public rule func_param_definition = seq(type, identifier, seq(EQ, expression).or_push_null())
            .push($ -> new Parameter($.$0(), $.$1(), $.$2()));

    public rule func_params_definition = seq(LPAREN, func_param_definition.sep(0, COMMA), RPAREN)
            .push(ActionContext::$list);

    public rule return_statement = seq(_ret, expression.sep(0, COMMA))
            .push($ -> new Return($.$list()));

    public rule function_definition = seq(_func, type, identifier, func_params_definition, _block)
            .push($ -> new Function($.$0(), $.$1(), $.$2(), $.$3()));

    public rule statement = choice(
            if_statement,
            while_statement,
            for_in_statement,
            var_definition,
            function_definition,
            return_statement,
            expression_statement
    );

    public rule block = seq(
            LBRACE,
            seq(return_statement.not(), _statement).at_least(0),
            return_statement.opt(),
            RBRACE
    ).push($ -> new Block($.$list()));

    public rule root = seq(ws, statement.at_least(1))
            .push($ -> new Root($.$list()));

    @Override
    public rule root() {
        return root;
    }

}
