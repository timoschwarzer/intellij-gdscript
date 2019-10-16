package gdscript.parser

import com.intellij.lang.ASTNode
import com.intellij.lang.PsiParser
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet
import gdscript.GDScriptLexer
import gdscript.GDScriptParser
import gdscript.token.ScriptTokenSet.LINE_COMMENTS
import gdscript.token.ScriptTokenSet.STRINGS
import gdscript.token.ScriptTokenSet.WHITESPACES
import gdscript.psi.RuleFactory
import gdscript.file.ScriptFile
import org.antlr.intellij.adaptor.lexer.ANTLRLexerAdaptor
import org.antlr.intellij.adaptor.lexer.PSIElementTypeFactory
import org.antlr.intellij.adaptor.parser.ANTLRParserAdaptor
import org.antlr.v4.runtime.Parser
import org.antlr.v4.runtime.tree.ParseTree


class ScriptParserDefinition : com.intellij.lang.ParserDefinition {

    init {
        @Suppress("DEPRECATION")
        PSIElementTypeFactory.defineLanguageIElementTypes(ScriptLanguage, GDScriptParser.tokenNames, GDScriptParser.ruleNames)
    }

    override fun createLexer(project: Project): Lexer =
        ANTLRLexerAdaptor(ScriptLanguage, GDScriptLexer(null))

    override fun createParser(project: Project): PsiParser =
        object : ANTLRParserAdaptor(ScriptLanguage, GDScriptParser(null)) {
            override fun parse(parser: Parser, root: IElementType): ParseTree =
                (parser as GDScriptParser).file()
        }

    override fun getWhitespaceTokens(): TokenSet = WHITESPACES

    override fun getCommentTokens(): TokenSet = LINE_COMMENTS

    override fun getStringLiteralElements(): TokenSet = STRINGS

    override fun getFileNodeType() = IFileElementType(ScriptLanguage)

    override fun createFile(view: FileViewProvider) = ScriptFile(view)

    override fun createElement(node: ASTNode) = RuleFactory.createPsiElement(node)

}