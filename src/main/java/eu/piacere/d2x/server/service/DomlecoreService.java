package eu.piacere.d2x.server.service;

import eu.piacere.d2x.server.api.DomlecoreApiDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import eu.piacere.doml.doml.commons.DOMLModel;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.lang.invoke.MethodHandles;

@Service
public class DomlecoreService implements DomlecoreApiDelegate {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Autowired
    public DomlecoreService() {
    }

    @Override
    public ResponseEntity<String> domlecoreGet() {
        // get file from inside a dependency "eu.piacere.doml" jar "/model/doml.ecore"
        InputStream inputStream = DOMLModel.class.getResourceAsStream("/model/doml.ecore");
        String inputStreamString = new java.util.Scanner(inputStream, "UTF-8").useDelimiter("\\A").next();
        return new ResponseEntity<>(inputStreamString, HttpStatus.OK);
    }
}
