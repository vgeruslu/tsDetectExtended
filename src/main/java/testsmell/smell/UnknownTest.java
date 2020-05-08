package testsmell.smell;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import testsmell.AbstractSmell;
import testsmell.SmellyElement;
import testsmell.TestMethod;
import testsmell.Util;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UnknownTest extends AbstractSmell {

    private List<SmellyElement> smellyElementList;
    private List<String> validUtilityMethods = new ArrayList<>();

    public UnknownTest() {
        smellyElementList = new ArrayList<>();
        validUtilityMethods = new ArrayList<>();
    }

    /**
     * Checks of 'Unknown Test' smell
     */
    @Override
    public String getSmellName() {
        return "Unknown Test";
    }

    /**
     * Returns true if any of the elements has a smell
     */
    @Override
    public boolean getHasSmell() {
        return smellyElementList.stream().filter(x -> x.getHasSmell()).count() >= 1;
    }

    /**
     * Analyze the test file for test methods that do not have assert statement or exceptions
     */
    @Override
    public void runAnalysis(CompilationUnit testFileCompilationUnit, CompilationUnit productionFileCompilationUnit, String testFileName, String productionFileName) throws FileNotFoundException {
        UnknownTest.ClassVisitor classVisitor;
        classVisitor = new UnknownTest.ClassVisitor();
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
       Edited by Niamh Duffy - Student 40178269 at Queen's University Belfast
       The smell is detected if the test method doesn't contain any assertion methods or utility methods which contain assertion methods.
    */

    private class ClassVisitor extends VoidVisitorAdapter<Void> {
        private MethodDeclaration currentMethod = null;
        TestMethod testMethod;
        List<String> assertMessage = new ArrayList<>();
        boolean hasAssert = false;
        boolean hasExceptionAnnotation = false;
        boolean containsValidUtilityMethod = false;


        // examine all methods in the test class

        @Override
        public void visit(MethodDeclaration n, Void arg) {
            if (Util.isTestMethod(n)) {
                Optional<AnnotationExpr> assertAnnotation = n.getAnnotationByName("Test");
                if (assertAnnotation.isPresent()) {
                    for (int i = 0; i < assertAnnotation.get().getNodeLists().size(); i++) {
                        NodeList<?> c = assertAnnotation.get().getNodeLists().get(i);
                        for (int j = 0; j < c.size(); j++)
                            if (c.get(j) instanceof MemberValuePair) {
                                if (((MemberValuePair) c.get(j)).getName().equals("expected") && ((MemberValuePair) c.get(j)).getValue().toString().contains("Exception"))
                                    ;
                                hasExceptionAnnotation = true;
                            }
                    }
                }
                currentMethod = n;
                testMethod = new TestMethod(n.getNameAsString());
                testMethod.setHasSmell(false); //default value is false (i.e. no smell)
                super.visit(n, arg);

                if (!hasAssert && !hasExceptionAnnotation && !containsValidUtilityMethod) {
                    testMethod.setHasSmell(true);
                }

                smellyElementList.add(testMethod);

                //reset values for next method
                currentMethod = null;
                assertMessage = new ArrayList<>();
                hasAssert = false;
                containsValidUtilityMethod = false;
            } else {
                for (int i = 0; i < n.getBody().get().getStatements().size(); i++){
                    if (n.getBody().get().getStatement(i).toString().startsWith("assertArrayEquals") ||
                            n.getBody().get().getStatement(i).toString().startsWith("assertEquals") ||
                            n.getBody().get().getStatement(i).toString().startsWith("assertNotSame") ||
                            n.getBody().get().getStatement(i).toString().startsWith("assertSame") ||
                            n.getBody().get().getStatement(i).toString().startsWith("assertThat") ||
                            n.getBody().get().getStatement(i).toString().startsWith("assertFalse") ||
                            n.getBody().get().getStatement(i).toString().startsWith("assertNotNull") ||
                            n.getBody().get().getStatement(i).toString().startsWith("assertNull") ||
                            n.getBody().get().getStatement(i).toString().startsWith("assertTrue")) {
                        hasAssert = true;
                        validUtilityMethods.add(n.getNameAsString());
                    }
                }
            }
        }

        // examine the methods being called within the test method
        @Override
        public void visit(MethodCallExpr n, Void arg) {
            super.visit(n, arg);
            if (currentMethod != null) {
                if (n.getNameAsString().startsWith(("assertArrayEquals")) ||
                        n.getNameAsString().startsWith(("assertEquals")) ||
                        n.getNameAsString().startsWith(("assertNotSame")) ||
                        n.getNameAsString().startsWith(("assertSame")) ||
                        n.getNameAsString().startsWith(("assertThat")) ||
                        n.getNameAsString().equals("assertFalse") ||
                        n.getNameAsString().equals("assertNotNull") ||
                        n.getNameAsString().equals("assertNull") ||
                        n.getNameAsString().equals("assertTrue")) {
                    hasAssert = true;
                    containsValidUtilityMethod = true;
                }
                else if (n.getNameAsString().equals("fail")) {
                    hasAssert = true;
                    containsValidUtilityMethod = true;
                }
                else if (n.getNameAsString().startsWith("assert") && validUtilityMethods.contains(n.getNameAsString())) {
                    containsValidUtilityMethod = true;
                }
            }
        }

    }
}

