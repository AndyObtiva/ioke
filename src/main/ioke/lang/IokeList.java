/*
 * See LICENSE file in distribution for copyright and licensing information.
 */
package ioke.lang;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

import ioke.lang.exceptions.ControlFlow;

/**
 *
 * @author <a href="mailto:ola.bini@gmail.com">Ola Bini</a>
 */
public class IokeList extends IokeData {
    private List<Object> list;

    public IokeList() {
        this(new ArrayList<Object>());
    }

    public IokeList(List<Object> l) {
        this.list = l;
    }

    public static void add(Object list, Object obj) {
        ((IokeList)IokeObject.data(list)).list.add(obj);
    }

    @Override
    public void init(IokeObject obj) throws ControlFlow {
        final Runtime runtime = obj.runtime;

        obj.setKind("List");
        obj.mimics(IokeObject.as(runtime.mixins.getCell(null, null, "Enumerable")), runtime.nul, runtime.nul);

        obj.registerMethod(obj.runtime.newJavaMethod("Returns a text inspection of the object", new JavaMethod("inspect") {
                @Override
                public Object activate(IokeObject method, IokeObject context, IokeObject message, Object on) throws ControlFlow {
                    return method.runtime.newText(IokeList.getInspect(on));
                }
            }));

        obj.registerMethod(obj.runtime.newJavaMethod("Returns a brief text inspection of the object", new JavaMethod("notice") {
                @Override
                public Object activate(IokeObject method, IokeObject context, IokeObject message, Object on) throws ControlFlow {
                    return method.runtime.newText(IokeList.getNotice(on));
                }
            }));
        
        obj.registerMethod(runtime.newJavaMethod("takes either one or two or three arguments. if one argument is given, it should be a message chain that will be sent to each object in the list. the result will be thrown away. if two arguments are given, the first is an unevaluated name that will be set to each of the values in the list in succession, and then the second argument will be evaluated in a scope with that argument in it. if three arguments is given, the first one is an unevaluated name that will be set to the index of each element, and the other two arguments are the name of the argument for the value, and the actual code. the code will evaluate in a lexical context, and if the argument name is available outside the context, it will be shadowed. the method will return the list.", new JavaMethod("each") {
                @Override
                public Object activate(IokeObject method, IokeObject context, IokeObject message, Object on) throws ControlFlow {
                    Runtime runtime = context.runtime;
                    List<Object> ls = ((IokeList)IokeObject.data(on)).list;
                    switch(message.getArgumentCount()) {
                    case 1: {
                        IokeObject code = IokeObject.as(message.getArguments().get(0));

                        for(Object o : ls) {
                            code.evaluateCompleteWithReceiver(context, context.getRealContext(), o);
                        }
                        break;
                    }
                    case 2: {
                        LexicalContext c = new LexicalContext(context.runtime, context, "Lexical activation context for List#each", message, context);
                        String name = IokeObject.as(message.getArguments().get(0)).getName();
                        IokeObject code = IokeObject.as(message.getArguments().get(1));

                        for(Object o : ls) {
                            c.setCell(name, o);
                            code.evaluateCompleteWithoutExplicitReceiver(c, c.getRealContext());
                        }
                        break;
                    }
                    case 3: {
                        LexicalContext c = new LexicalContext(context.runtime, context, "Lexical activation context for List#each", message, context);
                        String iname = IokeObject.as(message.getArguments().get(0)).getName();
                        String name = IokeObject.as(message.getArguments().get(1)).getName();
                        IokeObject code = IokeObject.as(message.getArguments().get(2));

                        int index = 0;
                        for(Object o : ls) {
                            c.setCell(name, o);
                            c.setCell(iname, runtime.newNumber(index++));
                            code.evaluateCompleteWithoutExplicitReceiver(c, c.getRealContext());
                        }
                        break;
                    }
                    }
                    return on;
                }
            }));

        obj.registerMethod(runtime.newJavaMethod("takes one argument and adds it at the end of the list, and then returns the list", new JavaMethod("<<") {
                @Override
                public Object activate(IokeObject method, IokeObject context, IokeObject message, Object on) throws ControlFlow {
                    Object arg = message.getEvaluatedArgument(0, context);
                    IokeList.add(on, arg);
                    return on;
                }
            }));

        obj.registerMethod(runtime.newJavaMethod("will remove all the entries from the list, and then returns the list", new JavaMethod("clear!") {
                @Override
                public Object activate(IokeObject method, IokeObject context, IokeObject message, Object on) throws ControlFlow {
                    ((IokeList)IokeObject.data(on)).getList().clear();
                    return on;
                }
            }));

        obj.registerMethod(runtime.newJavaMethod("returns true if this list is empty, false otherwise", new JavaMethod("empty?") {
                @Override
                public Object activate(IokeObject method, IokeObject context, IokeObject message, Object on) throws ControlFlow {
                    return ((IokeList)IokeObject.data(on)).getList().isEmpty() ? context.runtime._true : context.runtime._false;
                }
            }));

        obj.registerMethod(runtime.newJavaMethod("returns a new sorted version of this list", new JavaMethod("sort") {
                @Override
                public Object activate(IokeObject method, IokeObject context, IokeObject message, Object on) throws ControlFlow {
                    Object newList = IokeObject.mimic(on, message, context);
                    try {
                        Collections.sort(((IokeList)IokeObject.data(newList)).getList(), new SpaceshipComparator(context, message));
                    } catch(RuntimeException e) {
                        if(e.getCause() instanceof ControlFlow) {
                            throw (ControlFlow)e.getCause();
                        }
                        throw e;
                    }
                    return newList;
                }
            }));

        obj.registerMethod(runtime.newJavaMethod("sorts this list in place and then returns it", new JavaMethod("sort!") {
                @Override
                public Object activate(IokeObject method, IokeObject context, IokeObject message, Object on) throws ControlFlow {
                    try {
                        Collections.sort(((IokeList)IokeObject.data(on)).getList(), new SpaceshipComparator(context, message));
                    } catch(RuntimeException e) {
                        if(e.getCause() instanceof ControlFlow) {
                            throw (ControlFlow)e.getCause();
                        }
                        throw e;
                    }
                    return on;
                }
            }));

        obj.registerMethod(runtime.newJavaMethod("returns the size of this list", new JavaMethod("size") {
                @Override
                public Object activate(IokeObject method, IokeObject context, IokeObject message, Object on) throws ControlFlow {
                    return context.runtime.newNumber(((IokeList)IokeObject.data(on)).getList().size());
                }
            }));
        obj.aliasMethod("size", "length");

        obj.registerMethod(runtime.newJavaMethod("takes one argument, the index of the element to be returned. can be negative, and will in that case return indexed from the back of the list. if the index is outside the bounds of the list, will return nil. the argument can also be a range, and will in that case interpret the first index as where to start, and the second the end. the end can be negative and will in that case be from the end. if the first argument is negative, or after the second, an empty list will be returned. if the end point is larger than the list, the size of the list will be used as the end point.", new JavaMethod("at") {
                @Override
                public Object activate(IokeObject method, IokeObject context, IokeObject message, Object on) throws ControlFlow {
                    Object arg = message.getEvaluatedArgument(0, context);

                    if(IokeObject.data(arg) instanceof Range) {
                        int first = Number.extractInt(Range.getFrom(arg), message, context); 
                        
                        if(first < 0) {
                            return context.runtime.newList(new ArrayList<Object>());
                        }

                        int last = Number.extractInt(Range.getTo(arg), message, context);
                        boolean inclusive = Range.isInclusive(arg);

                        List<Object> o = ((IokeList)IokeObject.data(on)).getList();
                        int size = o.size();

                        if(last < 0) {
                            last = size + last;
                        }

                        if(last < 0) {
                            return context.runtime.newList(new ArrayList<Object>());
                        }

                        if(last >= size) {
                            
                            last = inclusive ? size-1 : size;
                        }

                        if(first > last || (!inclusive && first == last)) {
                            return context.runtime.newList(new ArrayList<Object>());
                        }
                        
                        if(!inclusive) {
                            last--;
                        }
                        
                        return context.runtime.newList(new ArrayList<Object>(o.subList(first, last+1)));
                    }

                    if(!(IokeObject.data(arg) instanceof Number)) {
                        arg = IokeObject.convertToNumber(arg, message, context);
                    }
                    int index = ((Number)IokeObject.data(arg)).asJavaInteger();
                    List<Object> o = ((IokeList)IokeObject.data(on)).getList();
                    if(index < 0) {
                        index = o.size() + index;
                    }

                    if(index >= 0 && index < o.size()) {
                        return o.get((int)index);
                    } else {
                        return context.runtime.nil;
                    }
                }
            }));

        obj.aliasMethod("at", "[]");

        obj.registerMethod(runtime.newJavaMethod("takes two arguments, the index of the element to set, and the value to set. the index can be negative and will in that case set indexed from the end of the list. if the index is larger than the current size, the list will be expanded with nils. an exception will be raised if a abs(negative index) is larger than the size.", new JavaMethod("at=") {
                @Override
                public Object activate(IokeObject method, final IokeObject context, final IokeObject message, Object on) throws ControlFlow {
                    Object arg = message.getEvaluatedArgument(0, context);
                    Object value = message.getEvaluatedArgument(1, context);
                    if(!(IokeObject.data(arg) instanceof Number)) {
                        arg = IokeObject.convertToNumber(arg, message, context);
                    }
                    int index = ((Number)IokeObject.data(arg)).asJavaInteger();
                    List<Object> o = ((IokeList)IokeObject.data(on)).getList();
                    if(index < 0) {
                        index = o.size() + index;
                    }

                    while(index < 0) {
                        final IokeObject condition = IokeObject.as(IokeObject.getCellChain(context.runtime.condition, 
                                                                                           message, 
                                                                                           context, 
                                                                                           "Error", 
                                                                                           "Index")).mimic(message, context);
                        condition.setCell("message", message);
                        condition.setCell("context", context);
                        condition.setCell("receiver", on);
                        condition.setCell("index", context.runtime.newNumber(index));

                        final int[] newCell = new int[]{index};

                        context.runtime.withRestartReturningArguments(new RunnableWithControlFlow() {
                                public void run() throws ControlFlow {
                                    context.runtime.errorCondition(condition);
                                }}, 
                            context,
                            new Restart.ArgumentGivingRestart("useValue") { 
                                public IokeObject invoke(IokeObject context, List<Object> arguments) throws ControlFlow {
                                    newCell[0] = Number.extractInt(arguments.get(0), message, context);
                                    return context.runtime.nil;
                                }
                            }
                            );

                        index = newCell[0];
                        if(index < 0) {
                            index = o.size() + index;
                        }
                    }

                    if(index >= o.size()) {
                        int toAdd = (index-o.size()) + 1;
                        for(int i=0;i<toAdd;i++) {
                            o.add(context.runtime.nil);
                        }
                    }

                    o.set((int)index, value);

                    return value;
                }
            }));

        obj.aliasMethod("at=", "[]=");
    }

    public void add(Object obj) {
        list.add(obj);
    }

    public List<Object> getList() {
        return list;
    }

    public static List<Object> getList(Object on) {
        return ((IokeList)(IokeObject.data(on))).getList();
    }

    public static String getInspect(Object on) throws ControlFlow {
        return ((IokeList)(IokeObject.data(on))).inspect(on);
    }

    public static String getNotice(Object on) throws ControlFlow {
        return ((IokeList)(IokeObject.data(on))).notice(on);
    }

    public IokeData cloneData(IokeObject obj, IokeObject m, IokeObject context) {
        return new IokeList(new ArrayList<Object>(list));
    }

    @Override
    public boolean isEqualTo(IokeObject self, Object other) {
        return ((other instanceof IokeObject) && 
                (IokeObject.data(other) instanceof IokeList) 
                && this.list.equals(((IokeList)IokeObject.data(other)).list));
    }

    @Override
    public String toString() {
        return list.toString();
    }

    @Override
    public String toString(IokeObject obj) {
        return list.toString();
    }

    public String inspect(Object obj) throws ControlFlow {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        String sep = "";
        for(Object o : list) {
            sb.append(sep).append(IokeObject.inspect(o));
            sep = ", ";
        }
        sb.append("]");
        return sb.toString();
    }

    public String notice(Object obj) throws ControlFlow {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        String sep = "";
        for(Object o : list) {
            sb.append(sep).append(IokeObject.notice(o));
            sep = ", ";
        }
        sb.append("]");
        return sb.toString();
    }
}// IokeList
