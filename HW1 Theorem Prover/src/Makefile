JFLAGS = -g
JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
	Variable.java \
	TheoremProver.java \

classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class