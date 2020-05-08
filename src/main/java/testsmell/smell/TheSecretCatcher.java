package testsmell.smell;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.ThrowStmt;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import testsmell.AbstractSmell;
import testsmell.SmellyElement;
import testsmell.TestMethod;
import testsmell.Util;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

/*
This class checks if test methods in the class either catch or throw exceptions. Use Junit's exception handling to automatically pass/fail the test
If this code detects the existence of a catch block or a throw statement in the methods body, the method is marked as smelly
 */
public class TheSecretCatcher extends AbstractSmell {

    private List<SmellyElement> smellyElementList;

    public TheSecretCatcher() {
        smellyElementList = new ArrayList<>();
    }

    /**
     * Checks of 'The Secret Catcher' smell
     */
    @Override
    public String getSmellName() {
        return "The Secret Catcher";
    }

    /**
     * Returns true if any of the elements has a smell
     */
    @Override
    public boolean getHasSmell() {
        return smellyElementList.stream().filter(x -> x.getHasSmell()).count() >= 1;
    }

    /**
     * Analyze the test file for test methods that have exception handling
     */
    @Override
    public void runAnalysis(CompilationUnit testFileCompilationUnit, CompilationUnit productionFileCompilationUnit, String testFileName, String productionFileName) throws FileNotFoundException {
        TheSecretCatcher.ClassVisitor classVisitor;
        classVisitor = new TheSecretCatcher.ClassVisitor();
        classVisitor.visit(testFileCompilationUnit, null);
    }

    /**
     * Returns the set of analyzed elements (i.e. test methods)
     */
    @Override
    public List<SmellyElement> getSmellyElements() {
        return smellyElementList;
    }

    private class ClassVisitor extends VoidVisitorAdapter<Void> {
        private MethodDeclaration currentMethod = null;
        TestMethod testMethod;


        // examine all methods in the test class
        @Override
        public void visit(MethodDeclaration n, Void arg) {
            if (Util.isTestMethod(n)) {
                currentMethod = n;
                testMethod = new TestMethod(n.getNameAsString());
                testMethod.setHasSmell(false); //default value is false (i.e. no smell)
                super.visit(n, arg);

                /*
                    Edited by Niamh Duffy - Student 40178269 at Queen's University Belfast
                    The smell is detected if the test method throws an exception and does not contain any assertion messages
                    therefore the exception needs to be thrown in order to fail the test, and the test passes otherwise.
                 */
                for (int i = 0; i < n.getBody().get().getStatements().size(); i++) {
                    if ((n.getThrownExceptions().size() >= 1 || n.getBody().get().getStatement(0).toString().contains("try")) &&
                            (n.getBody().get().getStatement(i).toString().contains("fail") &&
                                    (!n.getBody().get().getStatement(i).toString().startsWith("assertArrayEquals") ||
                                            !n.getBody().get().getStatement(i).toString().startsWith("assertEquals") ||
                                            !n.getBody().get().getStatement(i).toString().startsWith("assertNotSame") ||
                                            !n.getBody().get().getStatement(i).toString().startsWith("assertSame") ||
                                            !n.getBody().get().getStatement(i).toString().startsWith("assertThat") ||
                                            !n.getBody().get().getStatement(i).toString().startsWith("assertFalse") ||
                                            !n.getBody().get().getStatement(i).toString().startsWith("assertNotNull") ||
                                            !n.getBody().get().getStatement(i).toString().startsWith("assertNull") ||
                                            !n.getBody().get().getStatement(i).toString().startsWith("assertTrue"))))
                        testMethod.setHasSmell(true);
                }
                smellyElementList.add(testMethod);
            }

            //reset values for next method
            currentMethod = null;
        }

    }
}
