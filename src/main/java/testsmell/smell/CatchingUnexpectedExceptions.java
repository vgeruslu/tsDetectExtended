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
   Added by Niamh Duffy - Student 40178269 at Queen's University Belfast
   The smell is detected if the test method throws a general Exception i.e. will pass if any exception is thrown.
*/
public class CatchingUnexpectedExceptions extends AbstractSmell {

    private List<SmellyElement> smellyElementList;

    public CatchingUnexpectedExceptions() {
        smellyElementList = new ArrayList<>();
    }

    /**
     * Checks of 'Catching Unexpected Exceptions' smell
     */
    @Override
    public String getSmellName() {
        return "Catching Unexpected Exceptions";
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
        CatchingUnexpectedExceptions.ClassVisitor classVisitor;
        classVisitor = new CatchingUnexpectedExceptions.ClassVisitor();
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

        @Override
        public void visit(MethodDeclaration n, Void arg) {
            if (Util.isTestMethod(n)) {
                testMethod = new TestMethod(n.getNameAsString());
                testMethod.setHasSmell(false); //default value is false (i.e. no smell)
                super.visit(n, arg);

                for (int i = 0; i < n.getBody().get().getStatements().size(); i++) {
                    for (int j = 0; j < n.getThrownExceptions().size(); j++) {
                        if (n.getThrownException(j).toString().equals("Exception")) {
                            testMethod.setHasSmell(true);
                        }
                    }
                    if (n.getBody().get().getStatement(i).toString().contains("catch (Exception ")) {
                        testMethod.setHasSmell(true);
                    }
                }
                smellyElementList.add(testMethod);
            }

        }

    }
}
