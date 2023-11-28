package com.sandro;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.Set;

@SupportedAnnotationTypes("com.sandro.Get")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class GetProcessor extends AbstractProcessor {

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        StringBuilder builder = new StringBuilder()
                .append("package aboutjava.annotion;\n\n")
                .append("public class GeneratedClass {\n\n") // open class
                .append("\tpublic String getMessage() {\n") // open method
                .append("\t\treturn \"");
        // for each javax.lang.model.element.Element annotated with the CustomAnnotation
        for (Element element : roundEnv.getElementsAnnotatedWith(Get.class)) {
            String objectType = element.getSimpleName().toString(); // this is appending to the return statement
            builder.append(objectType).append(" says hello!\\n");
        }
        builder.append("\";\n") // end return
                .append("\t}\n") // close method16
                .append("}\n"); // close class17
        try { // write the file18
            JavaFileObject source = processingEnv.getFiler().createSourceFile("com.sandro.GeneratedClass");
            Writer writer = source.openWriter();
            writer.write(builder.toString());
            writer.flush();
            writer.close();
        } catch (IOException e) {
            // Note: calling e.printStackTrace() will print IO errors25
            // that occur from the file already existing after its first run, this is normal26
        }
        return true;
    }

}
