package eu.piacere.d2x.server.service;

import eu.piacere.d2x.server.api.Doml2domlxApiDelegate;
import eu.piacere.d2x.server.api.Domlx2domlApiDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;

@Service
public class Domlx2domlService implements Domlx2domlApiDelegate {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Autowired
    public Domlx2domlService() {
    }

    @Override
    public ResponseEntity<String> domlx2domlPost(String body) {
        return new ResponseEntity<>("not implemented", HttpStatus.NOT_IMPLEMENTED);
    }
}
