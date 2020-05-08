package testsmell.smell;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import testsmell.AbstractSmell;
import testsmell.SmellyElement;
import testsmell.TestMethod;
import testsmell.Util;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class DuplicateTestCode extends AbstractSmell {

    /*
        Added by Niamh Duffy - Student 40178269 at Queen's University Belfast
        Even when separate test methods are testing different things but in a different way,
        duplicated code from other test methods makes the test difficult to maintain. This is
        because there are more lines of test code which may need updated if the SUT is modified
        in such a way to modify the behaviour of the tests, therefore taking more time and effort to
        update the test code. This smell is marked as present if there is a test method in the class
        which contains 3 or more statements which are also present in previous test methods.
    */

    private List<SmellyElement> smellyElementList;

    public DuplicateTestCode() {
        smellyElementList = new ArrayList<>();
    }

    /**
     * Checks of 'Duplicate Assert' smell
     */
    @Override
    public String getSmellName() {
        return "Duplicate Test Code";
    }

    /**
     * Returns true if any of the elements has a smell
     */
    @Override
    public boolean getHasSmell() {
        return smellyElementList.stream().filter(x -> x.getHasSmell()).count() >= 1;
    }

    /**
     * Analyze the test file for test methods that have multiple assert statements with the same explanation message
     */
    @Override
    public void runAnalysis(CompilationUnit testFileCompilationUnit, CompilationUnit productionFileCompilationUnit, String testFileName, String productionFileName) throws FileNotFoundException {
        DuplicateTestCode.ClassVisitor classVisitor;
        classVisitor = new DuplicateTestCode.ClassVisitor();
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
        int duplicateCount = 0;
        List<String> statements = new ArrayList<>();

        @Override
        public void visit(MethodDeclaration n, Void arg) {
            if (Util.isTestMethod(n)) {
                currentMethod = n;
                testMethod = new TestMethod(n.getNameAsString());
                testMethod.setHasSmell(false);
                super.visit(n, arg);

                for (int i = 0; i < n.getBody().get().getStatements().size(); i++) {
                    // Check if any of the statements in the test are duplicated from previous test methods
                    String statement = n.getBody().get().getStatement(i).toString();
                    statement = statement.replaceAll("\\s", "");
                    for (int j = 0; j < statements.size(); j++){
                        if (statements.contains(statement)){
                            duplicateCount++;
                        }
                    }

                    statements.add(statement);
                    // if there are more than 3 duplicated statements, mark the test as smelly
                    if (duplicateCount >= 3){
                        testMethod.setHasSmell(true);
                    }
                }

                smellyElementList.add(testMethod);
                currentMethod = null;
                duplicateCount = 0;
            }
        }

    }
}

