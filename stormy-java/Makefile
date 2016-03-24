DEST := stormy-java
module: prepare $(DEST)/Supervisor.class $(DEST)/Worker.class $(DEST)/Time.class $(DEST)/Utils.class

prepare:
	mkdir -p $(DEST)

$(DEST)/%.class: %.java
	javac -d $(DEST) -target 7 -source 7 $^

clean:
	rm -rf $(DEST)
