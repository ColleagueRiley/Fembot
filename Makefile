GXX = javac
SOURCE = source/
ARGS = 
LIBS = 
OUTPUT = Fembot
RUN = java

all:
	@cd $(SOURCE) && $(GXX) ./*java $(ARGS) $(LIBS) 
	@cd source && $(RUN) $(OUTPUT)

build: 
	@cd $(SOURCE) && $(GXX) ./*java $(ARGS) $(LIBS) 

run: 
	@cd source && $(RUN) $(OUTPUT)