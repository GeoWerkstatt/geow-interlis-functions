package ch.geowerkstatt.ilivalidator.extensions.functions;

import antlr.RecognitionException;
import antlr.TokenStream;
import antlr.TokenStreamException;
import ch.interlis.ili2c.metamodel.Evaluable;
import ch.interlis.ili2c.metamodel.ExpressionSelection;
import ch.interlis.ili2c.metamodel.Selection;
import ch.interlis.ili2c.metamodel.TransferDescription;
import ch.interlis.ili2c.metamodel.Viewable;
import ch.interlis.ili2c.parser.Ili24Lexer;
import ch.interlis.ili2c.parser.Ili24Parser;

import java.io.StringReader;

public final class InterlisExpressionParser extends Ili24Parser {
    private InterlisExpressionParser(TransferDescription td, TokenStream lexer) {
        super(lexer);
        this.td = td;
    }

    /**
     * Create a new parser instance.
     *
     * @param td the {@link TransferDescription} instance.
     * @param expression the INTERLIS expression to parse.
     *
     * @return the new parser instance.
     */
    public static InterlisExpressionParser createParser(TransferDescription td, String expression) {
        Ili24Lexer lexer = new Ili24Lexer(new StringReader(expression));
        return new InterlisExpressionParser(td, lexer);
    }

    /**
     * Parse the given expression to an {@link Evaluable} condition.
     * The expression is expected to be a selection ({@code WHERE <logical-expression>;}).
     *
     * @param viewable the {@link Viewable} that represents the context of {@code THIS}.
     *
     * @return the parsed {@link Evaluable} condition.
     */
    public Evaluable parseWhereExpression(Viewable<?> viewable) {
        try {
            Selection selection = selection(viewable, viewable);
            return ((ExpressionSelection) selection).getCondition();
        } catch (RecognitionException | TokenStreamException e) {
            throw new RuntimeException(e);
        }
    }
}
