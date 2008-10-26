include_class('ioke.lang.Runtime') { 'IokeRuntime' } unless defined?(IokeRuntime)
include_class('ioke.lang.exceptions.MismatchedArgumentCount') unless defined?(MismatchedArgumentCount)
include_class('ioke.lang.exceptions.ArgumentWithoutDefaultValue') unless defined?(ArgumentWithoutDefaultValue)

import Java::java.io.StringReader unless defined?(StringReader)

describe "DefaultBehavior" do 
  describe "'method'" do 
    it "should return a method that returns nil when called with no arguments" do 
      ioke = IokeRuntime.get_runtime()
      ioke.evaluate_stream(StringReader.new("method call")).should == ioke.nil
      ioke.evaluate_stream(StringReader.new("method() call")).should == ioke.nil
    end
    
    it "should name itself after the slot it's assigned to if it has no name" do 
      ioke = IokeRuntime.get_runtime()
      ioke.evaluate_stream(StringReader.new("x = method(nil)")).data.name.should == "x"
    end
    
    it "should not change it's name if it already has a name" do 
      ioke = IokeRuntime.get_runtime()
      ioke.evaluate_stream(StringReader.new("x = method(nil)\ny = cell(\"x\")\ncell(\"y\")")).data.name.should == "x"
    end
    
    it "should know it's own name" do 
      ioke = IokeRuntime.get_runtime()
      ioke.evaluate_stream(StringReader.new("(x = method(nil)) name")).data.text.should == "x"
    end
  end
end

describe "DefaultMethod" do 
  it "should be possible to give it a documentation string" do 
    ioke = IokeRuntime.get_runtime()
    ioke.evaluate_stream(StringReader.new("method(\"foo is bar\", nil) documentation")).data.text.should == "foo is bar"
  end
  
  it "should report arity failures with regular arguments" do 
    ioke = IokeRuntime.get_runtime()
    ioke.evaluate_stream(StringReader.new(<<CODE))
noargs = method(nil)
onearg = method(x, nil)
twoargs = method(x, y, nil)
CODE

    proc do 
      ioke.evaluate_stream(StringReader.new("noargs(1)"))
    end.should raise_error(MismatchedArgumentCount)

    proc do 
      ioke.evaluate_stream(StringReader.new("onearg"))
    end.should raise_error(MismatchedArgumentCount)

    proc do 
      ioke.evaluate_stream(StringReader.new("onearg()"))
    end.should raise_error(MismatchedArgumentCount)

    proc do 
      ioke.evaluate_stream(StringReader.new("onearg(1, 2)"))
    end.should raise_error(MismatchedArgumentCount)

    proc do 
      ioke.evaluate_stream(StringReader.new("twoargs"))
    end.should raise_error(MismatchedArgumentCount)

    proc do 
      ioke.evaluate_stream(StringReader.new("twoargs()"))
    end.should raise_error(MismatchedArgumentCount)

    proc do 
      ioke.evaluate_stream(StringReader.new("twoargs(1)"))
    end.should raise_error(MismatchedArgumentCount)

    proc do 
      ioke.evaluate_stream(StringReader.new("twoargs(1, 2, 3)"))
    end.should raise_error(MismatchedArgumentCount)
  end

  it "should report arity failures with optional arguments" do 
    ioke = IokeRuntime.get_runtime()
    ioke.evaluate_stream(StringReader.new(<<CODE))
oneopt       = method(x 1, nil)
twoopt       = method(x 1, y 2, nil)
CODE

    proc do 
      ioke.evaluate_stream(StringReader.new("oneopt(1, 2)"))
    end.should raise_error(MismatchedArgumentCount)

    proc do 
      ioke.evaluate_stream(StringReader.new("twoopt(1, 2, 3)"))
    end.should raise_error(MismatchedArgumentCount)
  end

  it "should report arity failures with regular and optional arguments" do 
    ioke = IokeRuntime.get_runtime()
    ioke.evaluate_stream(StringReader.new(<<CODE))
oneopt       = method(y, x 1, nil)
twoopt       = method(z, x 1, y 2, nil)
oneopttworeg = method(z, q, x 1, nil)
twoopttworeg = method(z, q, x 1, y 2, nil)
CODE

    proc do 
      ioke.evaluate_stream(StringReader.new("oneopt"))
    end.should raise_error(MismatchedArgumentCount)

    proc do 
      ioke.evaluate_stream(StringReader.new("oneopt()"))
    end.should raise_error(MismatchedArgumentCount)

    proc do 
      ioke.evaluate_stream(StringReader.new("oneopt(1,2,3)"))
    end.should raise_error(MismatchedArgumentCount)
    
    proc do 
      ioke.evaluate_stream(StringReader.new("twoopt"))
    end.should raise_error(MismatchedArgumentCount)

    proc do 
      ioke.evaluate_stream(StringReader.new("twoopt()"))
    end.should raise_error(MismatchedArgumentCount)

    proc do 
      ioke.evaluate_stream(StringReader.new("twoopt(1,2,3,4)"))
    end.should raise_error(MismatchedArgumentCount)
    
    proc do 
      ioke.evaluate_stream(StringReader.new("oneopttworeg"))
    end.should raise_error(MismatchedArgumentCount)

    proc do 
      ioke.evaluate_stream(StringReader.new("oneopttworeg()"))
    end.should raise_error(MismatchedArgumentCount)

    proc do 
      ioke.evaluate_stream(StringReader.new("oneopttworeg(1)"))
    end.should raise_error(MismatchedArgumentCount)

    proc do 
      ioke.evaluate_stream(StringReader.new("oneopttworeg(1,2,3,4)"))
    end.should raise_error(MismatchedArgumentCount)

    proc do 
      ioke.evaluate_stream(StringReader.new("twoopttworeg(1,2,3,4,5)"))
    end.should raise_error(MismatchedArgumentCount)
  end
  
  it "should report mismatched arguments when trying to define optional arguments before regular ones" do 
    ioke = IokeRuntime.get_runtime()
    proc do 
      ioke.evaluate_stream(StringReader.new(<<CODE))
method(x 1, y, nil)
CODE
    end.should raise_error(ArgumentWithoutDefaultValue)
  end
    
  it "should be possible to give it one optional argument with simple data" do 
    ioke = IokeRuntime.get_runtime()
    ioke.evaluate_stream(StringReader.new(<<CODE))
m = method(x 42, x)
CODE
    ioke.evaluate_stream(StringReader.new("m")).data.as_java_integer.should == 42
    ioke.evaluate_stream(StringReader.new("m(43)")).data.as_java_integer.should == 43
  end

  it "should be possible to give it one optional argument and one regular argument with simple data" do 
    ioke = IokeRuntime.get_runtime()
    ioke.evaluate_stream(StringReader.new(<<CODE))
first = method(x, y 42, x)
second = method(x, y 42, y)
CODE

    ioke.evaluate_stream(StringReader.new("first(10)")).data.as_java_integer.should == 10
    ioke.evaluate_stream(StringReader.new("second(10)")).data.as_java_integer.should == 42

    ioke.evaluate_stream(StringReader.new("first(10, 13)")).data.as_java_integer.should == 10
    ioke.evaluate_stream(StringReader.new("second(10, 13)")).data.as_java_integer.should == 13
  end
  
  it "should be possible to give it one regular argument and one optional argument that refers to the first one" do 
    ioke = IokeRuntime.get_runtime()
    ioke.evaluate_stream(StringReader.new(<<CODE))
first = method(x, y x + 42, y)
CODE
    ioke.evaluate_stream(StringReader.new("first(10)")).data.as_java_integer.should == 52
    ioke.evaluate_stream(StringReader.new("first(10, 33)")).data.as_java_integer.should == 33
  end
  
  it "should be possible to give it two optional arguments where the second refers to the first one" do 
    ioke = IokeRuntime.get_runtime()
    ioke.evaluate_stream(StringReader.new(<<CODE))
first  = method(x 13, y x + 42, x)
second = method(x 13, y x + 42, y)
CODE
    ioke.evaluate_stream(StringReader.new("first")).data.as_java_integer.should == 13
    ioke.evaluate_stream(StringReader.new("first(10)")).data.as_java_integer.should == 10
    ioke.evaluate_stream(StringReader.new("first(10, 444)")).data.as_java_integer.should == 10

    ioke.evaluate_stream(StringReader.new("second")).data.as_java_integer.should == 55
    ioke.evaluate_stream(StringReader.new("second(10)")).data.as_java_integer.should == 52
    ioke.evaluate_stream(StringReader.new("second(10, 444)")).data.as_java_integer.should == 444
  end
  
  it "should be possible to have more complicated expression as default value" do 
    ioke = IokeRuntime.get_runtime()
    ioke.evaluate_stream(StringReader.new(<<CODE))
first  = method(x 13, y "foo";(x + 42)-1, y)
CODE

    ioke.evaluate_stream(StringReader.new("first")).data.as_java_integer.should == 54
    ioke.evaluate_stream(StringReader.new("first(12)")).data.as_java_integer.should == 53
    ioke.evaluate_stream(StringReader.new("first(12, 52)")).data.as_java_integer.should == 52
  end
end
