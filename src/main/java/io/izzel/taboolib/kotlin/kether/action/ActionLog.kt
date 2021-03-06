package io.izzel.taboolib.kotlin.kether.action

import io.izzel.kether.common.api.ParsedAction
import io.izzel.kether.common.api.QuestAction
import io.izzel.kether.common.api.QuestContext
import io.izzel.kether.common.loader.types.ArgTypes
import io.izzel.taboolib.kotlin.kether.KetherParser
import io.izzel.taboolib.kotlin.kether.ScriptContext
import io.izzel.taboolib.kotlin.kether.ScriptParser
import java.util.concurrent.CompletableFuture

/**
 * @author IzzelAliz
 */
class ActionLog(val message: ParsedAction<*>) : QuestAction<Void>() {

    override fun process(context: QuestContext.Frame): CompletableFuture<Void> {
        return context.newFrame(message).run<Any>().thenAccept {
            if (context.context() is ScriptContext) {
                println(it.toString().trimIndent().replace("@sender", (context.context() as ScriptContext).sender?.name.toString()))
            } else {
                println(it.toString().trimIndent())
            }
        }
    }

    override fun toString(): String {
        return "ActionLog(message=$message)"
    }

    companion object {

        @KetherParser(["log", "print"])
        fun parser() = ScriptParser.parser {
            ActionLog(it.next(ArgTypes.ACTION))
        }
    }
}