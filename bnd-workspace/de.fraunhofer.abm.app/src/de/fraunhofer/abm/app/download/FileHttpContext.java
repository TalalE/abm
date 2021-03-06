package de.fraunhofer.abm.app.download;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.http.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.fraunhofer.abm.collection.dao.BuildResultDao;
import de.fraunhofer.abm.domain.BuildResultDTO;

public class FileHttpContext implements HttpContext {

    private static final transient Logger logger = LoggerFactory.getLogger(FileHttpContext.class);

    private BuildResultDao dao;
    private File archive;
    private String result;
    
    public FileHttpContext(BuildResultDao buildResultDao,String result) {
        this.dao = buildResultDao;
        this.result = result;
    }

    @Override
    public boolean handleSecurity(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // abusing this call to set response headers
        String buildResultId = request.getRequestURI().replaceAll(result+"/", "");
        BuildResultDTO buildResult = dao.findById(buildResultId);
        if(result=="/download") {
        archive = new File(buildResult.dir, "archive.zip");
        response.setContentLengthLong(archive.length());
        response.setHeader("Content-Disposition", "attachment; filename=archive.zip;");}
        return true; // allow all requests
    }

    @Override
    public URL getResource(String resource) {
        if(archive != null && archive.exists()) {
            try {
                return archive.toURI().toURL();
            } catch (MalformedURLException e) {
                logger.error("Couldn't create URL for archive download", e);
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public String getMimeType(String name) {
        return "application/zip";
    }
}
