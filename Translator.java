//package xtc.oop;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.BufferedWriter;
import java.io.FileWriter;

import xtc.lang.JavaFiveParser;

import xtc.parser.ParseException;
import xtc.parser.Result;

import xtc.tree.GNode;
import xtc.tree.Node;
import xtc.tree.Visitor;

import xtc.lang.javacc.syntaxtree.*; 

import xtc.util.Tool;

public class Translator extends Tool {

  public Translator() {  }
  public String getCopy() { return "(C) 2010"; }
  public String getName() { return "Java to C++ Translator"; }
  public String getExplanation() { return "This tool translates a subset of Java to a subset of C++.";}

  public void init() {
    super.init();

    // Declare command line arguments.
    runtime.
      bool("printJavaAST", "printJavaAST", false,
           "Print the Java AST.").
      bool("countMethods", "optionCountMethods", false,
           "Print the number of method declarations.");
  }

  public void prepare() {
    super.prepare(); // Perform consistency checks on command line arguments.
  }

  public File locate(String name) throws IOException {
    File file = super.locate(name);
    if (Integer.MAX_VALUE < file.length()) {
      throw new IllegalArgumentException(file + ": file too large");
    }
    return file;
  }

  public Node parse(Reader in, File file) throws IOException, ParseException { // returns parsed AST
    JavaFiveParser parser =
      new JavaFiveParser(in, file.toString(), (int)file.length());
    Result result = parser.pCompilationUnit(0);

    return (Node)parser.value(result);
  }

	// main translation method
  public void process(Node node) {
    if (runtime.test("printJavaAST")) {
      runtime.console().format(node).pln().flush();
	
    }

	  new Visitor() {
		  public void visitCompilationUnit(GNode n) {
			  System.out.println("Start of compilation unit");
        writeToFile("blah","/* Start of automatically generated C++ code */\n");
        visit(n);
		  }

      public void visitPackageDeclaration(GNode n) {
        System.out.println("TODO - Package Declaration");
        writeToFile("blah","-- package declaration here --\n");
        //visit(n);
      }

      public void visitImportDeclaration(GNode n) {
        System.out.println("TODO - Import Declaration");
        writeToFile("blah","-- #include <whatever> --\n");
        //visit(n);
      }

		  public void visitFieldDeclaration(GNode n) {    // Question: translate directly? separate into 3 functions?
        // START OF MODIFIERS
        Node modifiers = n.getNode(0);
        if (!modifiers.isEmpty()) {
          for (int i = 0; i < modifiers.size(); i++) {
            String modifier = modifiers.getNode(i).getString(0);
            if (modifier.equals("public") ||
                  modifier.equals("private") ||
                  modifier.equals("protected")) {
              writeToFile("blah", modifier + ":\n");
            } else if (modifier.equals("static") ||
                        modifier.equals("volatile")) {
              writeToFile("blah", modifier);
            } else if (modifier.equals("final")) {  // is final the same as const?
              writeToFile("blah", "const");
            } else {
              // "transient" or something unrecognized
            }
          }
        }
        // END OF MODIFIERS
			  // START OF TYPE
        Node type = n.getNode(1);
        if (!type.isEmpty()) {
          Node typeType = type.getNode(0);
          //if (typeType.getName().equals("PrimitiveType"))
          // to-do: check all types and convert to C++ equivalent
          writeToFile("blah", typeType.getString(0) + " ");
          if (type.getNode(1) != null) {
            // it's an array so we need to do all that array class shit
          }
        }
        // END OF TYPE
        // START OF DECLARATIONS
        Node declarations = n.getNode(2);
        if (!declarations.isEmpty()) {
          for (int i = 0; i < declarations.size(); i++) {
            Node declarator = declarations.getNode(i);
            if (declarator.getNode(1) == null) {
              writeToFile("blah", declarator.getString(0));
              if (declarator.getNode(2) != null) {
                writeToFile("blah", " = " + declarator.getNode(2).getString(0));
              }
              if ((i+1) < declarations.size())
                writeToFile("blah", ", ");
            } else {
               // its an array declaration
            }
          }
          writeToFile("blah", ";\n");
        }

        // END OF DECLARATIONS
        System.out.println(type);
		  }

		  public void visit (Node n) {
			  for (Object o : n) if (o instanceof Node) dispatch((Node)o);
		  }
	  }.dispatch(node);
  }

  private void writeToFile(String fileName, String content) {
    BufferedWriter output;
    // note: since we can have multiple files, we need to put this in a separate function and append, etc
    //int x = file.getName().lastIndexOf('.');
    // if (0 < x && x <= file.getName().length() - 2)

    try {
      output = new BufferedWriter(new FileWriter(fileName+".cpp",true));
      output.write(content);
      //output.newLine();
      output.close();
    } catch (Exception ex) { ; }
  }

  public static void main(String[] args) {
    new Translator().run(args);
  }

}

