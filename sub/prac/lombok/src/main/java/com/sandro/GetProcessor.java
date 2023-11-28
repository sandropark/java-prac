package com.sandro;

import com.google.auto.service.AutoService;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.util.List;
import java.util.Set;

@Slf4j
@SupportedAnnotationTypes("com.sandro.Get")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
@AutoService(Processor.class)
public class GetProcessor extends AbstractProcessor {
    private ProcessingEnvironment processingEnvironment;
    private Trees trees;
    private TreeMaker treeMaker;
    private Names names;
    private Context context;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        JavacProcessingEnvironment javacProcessingEnvironment = (JavacProcessingEnvironment) processingEnv;
        super.init(processingEnv);
        this.processingEnvironment = processingEnv;
        this.trees = Trees.instance(processingEnv);
        this.context = javacProcessingEnvironment.getContext();
        this.treeMaker = TreeMaker.instance(context);
        this.names = Names.instance(context);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        log.info("process 메서드 실행");

        TreePathScanner<Object, CompilationUnitTree> scanner = new TreePathScanner<Object, CompilationUnitTree>(){
            /**
             * CompillationUnitTree 는 소스파일에서 패키지 선언에서 부터 abstract syntax tree 를 정의함
             * ClassTree -> 클래스 , 인터페이스, enum 어노테이션을 트리노드로 선언
             * class 정의 위에 어노테이션 작성시 내부적으로 메소드 실행
             * CompilationUnitTree AST(Abstract Syntax Tree 의 최상단)
             */
            @Override
            public Trees visitClass(ClassTree classTree, CompilationUnitTree unitTree){
                JCTree.JCCompilationUnit compilationUnit = (JCTree.JCCompilationUnit) unitTree;
                // .java 파일인지 확인후 accept 를 통해 treeTransLator, 작성 메소드 생성
                if (compilationUnit.sourcefile.getKind() == JavaFileObject.Kind.SOURCE){
                    compilationUnit.accept(new TreeTranslator() {
                        @Override
                        public void visitClassDef(JCTree.JCClassDecl jcClassDecl) {
                            super.visitClassDef(jcClassDecl);
                            // Class 내부에 정의된 모든 member 를 싹다 가져옴.
                            List<JCTree> members = jcClassDecl.getMembers();
                            // Syntax tree 에서 모든 member 변수 얻음.
                            for(JCTree member : members){
                                if (member instanceof JCTree.JCVariableDecl){
                                    // member 변수에 대한 getter 메서드 생성.
                                    List<JCTree.JCMethodDecl> getters = createGetter((JCTree.JCVariableDecl) member);
                                    for(JCTree.JCMethodDecl getter : getters){
                                        jcClassDecl.defs = jcClassDecl.defs.prepend(getter);
                                    }
                                }
                            }
                        }
                    });
                }
                return trees;
            }
        };

        /**
         * RoundEnvironment
         * getElementsAnnotatedWith() -> @Get 의 어노테이션이 붙여져 있는 모든 element 를 불러 일으킨다.
         */
        for (final Element element : roundEnv.getElementsAnnotatedWith(Get.class)) {
            // 현재 어노테이션은 Type 이고 여기서 Class 뿐만 아니라, interface 와 enum 에도 작성이 가능하므로 class만 지정할 수 있도록
            if(element.getKind() != ElementKind.CLASS){
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "@Get annotation cant be used on" + element.getSimpleName());
            }else{
                processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "@Get annotation Processing " + element.getSimpleName());
                final TreePath path = trees.getPath(element);
                scanner.scan(path, path.getCompilationUnit());
            }
        }

        return true;
    }

    public List<JCTree.JCMethodDecl> createGetter(JCTree.JCVariableDecl var){
        // 필드 이름 변수에 앞문자 대문자로 변경 해주기
        String str = var.name.toString();
        String upperVar = str.substring(0,1).toUpperCase()+str.substring(1,var.name.length());

        return List.of(
                /**
                 * treeMaker.Modifiers -> syntax tree node 에 접근하여 수정및 삽입하는 역할
                 * @Parm : treeMaker.Modifiers flag 1-> public , 2-> private, 0-> default
                 * @Parm : methodName & Type, return 정의
                 */
                treeMaker.MethodDef(
                        treeMaker.Modifiers(1), // public
                        names.fromString("get".concat(upperVar)), // 메서드 명
                        (JCTree.JCExpression) var.getType(), // return type
                        List.nil(),
                        List.nil(),
                        List.nil(),
                        // 식생성 this.a = a;
                        treeMaker.Block(1, List.of(treeMaker.Return((treeMaker.Ident(var.getName()))))),
                        null));
    }
}
