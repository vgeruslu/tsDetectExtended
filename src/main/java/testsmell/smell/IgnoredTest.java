package testsmell.smell;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import testsmell.*;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class IgnoredTest extends AbstractSmell {

    private List<SmellyElement> smellyElementList;

    public IgnoredTest() {
        smellyElementList = new ArrayList<>();
    }

    /**
     * Checks of 'Ignored Test' smell
     */
    @Override
    public String getSmellName() {
        return "IgnoredTest";
    }

    /**
     * Returns true if any of the elements has a smell
     */
    @Override
    public boolean getHasSmell() {
        return smellyElementList.stream().filter(x -> x.getHasSmell()).count() >= 1;
    }

    /**
     * Analyze the test file for test methods that contain Ignored test methods
     */
    @Override
    public void runAnalysis(CompilationUnit testFileCompilationUnit, CompilationUnit productionFileCompilationUnit, String testFileName, String productionFileName) throws FileNotFoundException {
        IgnoredTest.ClassVisitor classVisitor;
        classVisitor = new IgnoredTest.ClassVisitor();
        classVisitor.visit(testFileCompilationUnit, null);
    }

    /**
     * Returns the set of analyzed elements (i.e. test methods)
     */
    @Override
    public List<SmellyElement> getSmellyElements() {
        return smellyElementList;
    }

    /*
       Edited by Niamh Duffy - Student 40178269 @ Queen's University Belfast
       Removed checks for JUnit 3 which does not have an @Ignore annotation present & return values for each
       test method in a file to determine if any, what methods are ignored.
    */
    private class ClassVisitor extends VoidVisitorAdapter<Void> {
        private MethodDeclaration currentMethod = null;
        TestMethod testMethod;

        @Override
        public void visit(MethodDeclaration n, Void arg) {
            if (Util.isTestMethod(n)){
                currentMethod = n;
                testMethod = new TestMethod(n.getNameAsString());
                testMethod.setHasSmell(false);
                if (n.getAnnotationByName("Ignore").isPresent()) {
                    testMethod.setHasSmell(true);
                }
                smellyElementList.add(testMethod);
                currentMethod = null;
            }
            super.visit(n, arg);
        }
    }
}
