package com.github.rainmanwy.robotframework.sikulilib;

import org.robotframework.javalib.library.RobotJavaLibrary;
import org.robotframework.javalib.library.KeywordDocumentationRepository;
import org.robotframework.javalib.library.AnnotationLibrary;
import org.robotframework.remoteserver.RemoteServer;

import com.github.rainmanwy.robotframework.sikulilib.utils.CaptureFolder;

/**
 * Created by Wang Yang on 2015/8/19.
 */
public class SikuliLibrary implements KeywordDocumentationRepository, RobotJavaLibrary {

    private final AnnotationLibrary annotationLibrary = new AnnotationLibrary("com/github/rainmanwy/robotframework/sikulilib/keywords/**/*.class");

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            throw new RuntimeException("Port number should be provided");
        }
        if (args.length >= 2) {
            CaptureFolder.getInstance().setCaptureFolder(args[1]);
        }
        RemoteServer.configureLogging();
        RemoteServer server = new RemoteServer();
        server.addLibrary(SikuliLibrary.class, Integer.parseInt(args[0]));
        server.start();
    }

    @Override
    public Object runKeyword(String keywordName, Object[] args)
    {
        return this.annotationLibrary.runKeyword(keywordName, toStrings(args));
    }

    @Override
    public String[] getKeywordNames()
    {
        return  this.annotationLibrary.getKeywordNames();
    }

    private Object[] toStrings(Object[] args) {
        Object[] newArgs = new Object[args.length];
        for (int i = 0; i < newArgs.length; i++) {
            if (args[i].getClass().isArray())
                newArgs[i] = args[i];
            else {
                newArgs[i] = args[i].toString();
            }
        }
        return newArgs;
    }

    @Override
    public String getKeywordDocumentation(String name) {
        return this.annotationLibrary.getKeywordDocumentation(name);
    }

    @Override
    public String[] getKeywordArguments(String name) {
        return this.annotationLibrary.getKeywordArguments(name);
    }
}
