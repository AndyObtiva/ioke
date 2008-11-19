/*
 * See LICENSE file in distribution for copyright and licensing information.
 */
package ioke.lang;

/**
 * The Ground serves the same purpose as the Lobby in Self and Io.
 * This is the place where everything is evaluated.
 *
 * @author <a href="mailto:ola.bini@gmail.com">Ola Bini</a>
 */
public class Ground {
    public static void init(IokeObject ground) {
        Runtime runtime = ground.runtime;
        ground.setKind("Ground");
        ground.registerCell("Base", runtime.base);
        ground.registerCell("DefaultBehavior", runtime.defaultBehavior);
        ground.registerCell("Ground", runtime.ground);
        ground.registerCell("Origin", runtime.origin);
        ground.registerCell("System", runtime.system);
        ground.registerCell("Runtime", runtime.runtime);
        ground.registerCell("Text", runtime.text);
        ground.registerCell("Symbol", runtime.symbol);
        ground.registerCell("Number", runtime.number);
        ground.registerCell("nil", runtime.nil);
        ground.registerCell("true", runtime._true);
        ground.registerCell("false", runtime._false);
        ground.registerCell("Method", runtime.method);
        ground.registerCell("DefaultMethod", runtime.defaultMethod);
        ground.registerCell("JavaMethod", runtime.javaMethod);
        ground.registerCell("LexicalBlock", runtime.lexicalBlock);
        ground.registerCell("DefaultMacro", runtime.defaultMacro);
        ground.registerCell("Mixins", runtime.mixins);
        ground.registerCell("Restart", runtime.restart);
        ground.registerCell("List", runtime.list);
        ground.registerCell("Dict", runtime.dict);
        ground.registerCell("Range", runtime.range);
        ground.registerCell("Pair", runtime.pair);
        ground.registerCell("Message", runtime.message);
        ground.registerCell("Call", runtime.call);
        ground.registerCell("Condition", runtime.condition);
        ground.registerCell("Rescue", runtime.rescue);
        ground.registerCell("Handler", runtime.handler);
        ground.registerCell("IO", runtime.io);
    }
}// Ground
