package testsmell.smell;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import testsmell.AbstractSmell;
import testsmell.SmellyElement;
import testsmell.Util;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class LonelyTest extends AbstractSmell {

    private List<SmellyElement> smellyElementList;
    private List<TestMethod> testMethods;


    public LonelyTest() {
        smellyElementList = new ArrayList<>();
        testMethods = new ArrayList<>();
    }

    /**
     * Checks of 'LonelyTest' smell
     */
    @Override
    public String getSmellName() {
        return "Lonely Test";
    }

    /**
     * Returns true if any of the elements has a smell
     */
    @Override
    public boolean getHasSmell() {
        return smellyElementList.stream().filter(x -> x.getHasSmell()).count() >= 1;
    }

    /**
     * Analyze the test file for test methods that call other test methods
     *
     * Edited by Niamh Duffy - Student 40178269 at Queen's University Belfast
     * Detection algorithm works as expected for the Lonely Test smell in that it detects when
     * a test method is called from another test method. Results are returned at both class-level
     * and method-level for whether or not the smell is present.
     */
    @Override
    public void runAnalysis(CompilationUnit testFileCompilationUnit, CompilationUnit productionFileCompilationUnit, String testFileName, String productionFileName) throws FileNotFoundException {
        LonelyTest.ClassVisitor classVisitor;
        classVisitor = new LonelyTest.ClassVisitor();
        classVisitor.visit(testFileCompilationUnit, null);

        for (TestMethod currentMethod : testMethods){
            testsmell.TestMethod testMethod = new testsmell.TestMethod(currentMethod.getMethodDeclaration().getNameAsString());
            testMethod.setHasSmell(false);
            for (int i = 0; i < currentMethod.getCalledMethods().size(); i++){
                String calledMethod = currentMethod.getCalledMethods().get(i).getName();
                for (int j = 0; j < testMethods.size(); j++){
                    if(testMethods.get(j).methodDeclaration.getName().toString().equals(calledMethod)){
                        testMethod.setHasSmell(true);
                    }
                }
            }
            smellyElementList.add(testMethod);
        }
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
        List<CalledMethod> calledMethods;

        // examine all methods in the test class
        @Override
        public void visit(MethodDeclaration n, Void arg) {
            if (Util.isTestMethod(n)) {
                currentMethod = n;
                calledMethods = new ArrayList<>();

                super.visit(n, arg);
                testMethods.add(new LonelyTest.TestMethod(n, calledMethods));
            }
        }

        // examine the methods being called within the test method
        @Override
        public void visit(MethodCallExpr n, Void arg) {
            super.visit(n, arg);
            if (currentMethod != null) {
                if (!calledMethods.contains(new CalledMethod(n.getArguments().size(), n.getNameAsString()))) {
                    calledMethods.add(new CalledMethod(n.getArguments().size(), n.getNameAsString()));
                }
            }
        }
    }

    private class TestMethod {
        public List<CalledMethod> getCalledMethods() {
            return calledMethods;
        }

        public MethodDeclaration getMethodDeclaration() {
            return methodDeclaration;
        }

        public TestMethod(MethodDeclaration methodDeclaration, List<CalledMethod> calledMethods) {
            this.methodDeclaration = methodDeclaration;
            this.calledMethods = calledMethods;
        }

        private List<CalledMethod> calledMethods;
        private MethodDeclaration methodDeclaration;
    }

    private class CalledMethod {
        public int getTotalArguments() {
            return totalArguments;
        }

        public String getName() {
            return name;
        }

        public CalledMethod(int totalArguments, String name) {
            this.totalArguments = totalArguments;
            this.name = name;
        }

        private int totalArguments;
        private String name;
    }
}
