package com.github.rainmanwy.robotframework.sikulilib;

import org.robotframework.javalib.library.RobotFrameworkDynamicAPI;
import org.robotframework.javalib.library.KeywordDocumentationRepository;
import org.robotframework.javalib.library.AnnotationLibrary;
import org.robotframework.remoteserver.RemoteServer;

import com.github.rainmanwy.robotframework.sikulilib.utils.CaptureFolder;

import java.util.List;
import java.util.Map;

/**
 * Created by Wang Yang on 2015/8/19.
 */
public class SikuliLibrary implements KeywordDocumentationRepository, RobotFrameworkDynamicAPI {

    private final AnnotationLibrary annotationLibrary = new AnnotationLibrary("com/github/rainmanwy/robotframework/sikulilib/keywords/**/*.class");

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            throw new RuntimeException("Port number should be provided");
        }
        if (args.length >= 2) {
            CaptureFolder.getInstance().setCaptureFolder(args[1]);
        }
        RemoteServer.configureLogging();
        RemoteServer server = new RemoteServer(Integer.parseInt(args[0]));
        server.putLibrary("/", new SikuliLibrary());
        server.start();
    }

    @Override
    public Object runKeyword(String keywordName, List args)
    {
        return this.annotationLibrary.runKeyword(keywordName, args);
    }

    @Override
    public Object runKeyword(String keywordName, List args, Map kwargs)
    {
        return this.annotationLibrary.runKeyword(keywordName, args, kwargs);
    }

    @Override
    public List<String> getKeywordNames()
    {
        return  this.annotationLibrary.getKeywordNames();
    }

    @Override
    public String getKeywordDocumentation(String name) {
        return this.annotationLibrary.getKeywordDocumentation(name);
    }

    @Override
    public List<String> getKeywordArguments(String name) {
        return this.annotationLibrary.getKeywordArguments(name);
    }
}
