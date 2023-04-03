package eu.piacere.d2x.server.service;

import eu.piacere.d2x.server.api.Doml2domlxApiDelegate;
import eu.piacere.doml.doml.application.ApplicationPackage;
import eu.piacere.doml.doml.commons.CommonsPackage;
import eu.piacere.doml.doml.commons.DOMLModel;
import eu.piacere.doml.doml.concrete.ConcretePackage;
import eu.piacere.doml.doml.infrastructure.InfrastructurePackage;
import eu.piacere.doml.doml.optimization.OptimizationPackage;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import eu.piacere.doml.DomlStandaloneSetup;
import org.eclipse.emf.common.util.URI;

import java.io.*;
import java.lang.invoke.MethodHandles;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.UUID;
// import org.eclipse.xtext.util.ReflectionUtil;
// import org.eclipse.xtext.xbase.lib.Functions.Function1;

@Service
public class Doml2domlxService implements Doml2domlxApiDelegate {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Autowired
    public Doml2domlxService() {
    }

    @Override
    public ResponseEntity<String> doml2domlxPost(String body) {

        String sourceExt = "doml";
        String targetExt = "domlx";

        DomlStandaloneSetup.doSetup();
        if (!EPackage.Registry.INSTANCE.containsKey(CommonsPackage.eNS_URI)) {
            EPackage.Registry.INSTANCE.put(CommonsPackage.eNS_URI, CommonsPackage.eINSTANCE);
        }
        if (!EPackage.Registry.INSTANCE.containsKey(ApplicationPackage.eNS_URI)) {
            EPackage.Registry.INSTANCE.put(ApplicationPackage.eNS_URI, ApplicationPackage.eINSTANCE);
        }
        if (!EPackage.Registry.INSTANCE.containsKey(InfrastructurePackage.eNS_URI)) {
            EPackage.Registry.INSTANCE.put(InfrastructurePackage.eNS_URI, InfrastructurePackage.eINSTANCE);
        }
        if (!EPackage.Registry.INSTANCE.containsKey(ConcretePackage.eNS_URI)) {
            EPackage.Registry.INSTANCE.put(ConcretePackage.eNS_URI, ConcretePackage.eINSTANCE);
        }
        if (!EPackage.Registry.INSTANCE.containsKey(OptimizationPackage.eNS_URI)) {
            EPackage.Registry.INSTANCE.put(OptimizationPackage.eNS_URI, OptimizationPackage.eINSTANCE);
        }
        if (!Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().containsKey("domlx")) {
            Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("domlx", new XMIResourceFactoryImpl());
        }

        String tmpDir = System.getProperty("java.io.tmpdir");
        // https://www.eclipse.org/forums/index.php/t/134790/
        // generate fake URI
        String sourceURIFakeString = getUniqueFileName(tmpDir, sourceExt);

        // create inputStream from body
        InputStream inputStream  = new ByteArrayInputStream(body.getBytes());

        ResourceSet resourceSet = new ResourceSetImpl();

        Resource inputResource = resourceSet.createResource(URI.createFileURI(sourceURIFakeString));

        try{
            inputResource.load(inputStream, null);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Error while loading doml into ecore resource", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        logger.info("doml loaded into ecore resource");

        String targetURIFakeString = getUniqueFileName(tmpDir, targetExt);

        Resource targetResource = resourceSet.createResource(URI.createFileURI(targetURIFakeString));
        try {
            DOMLModel model = (DOMLModel) inputResource.getContents().get(0);
            targetResource.getContents().add(EcoreUtil.copy(model));
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Error while creating domlx from ecore resource", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        logger.info("domlx created from ecore resource");

        // create outputStream from targetResource https://stackoverflow.com/a/1022434
        OutputStream outputStream = new OutputStream() {
            private StringBuilder string = new StringBuilder();

            @Override
            public void write(int b) throws IOException {
                this.string.append((char) b );
            }

            //Netbeans IDE automatically overrides this toString()
            public String toString() {
                return this.string.toString();
            }
        };

        try {
            targetResource.save(outputStream,null);
        } catch (Exception e) {
            e.printStackTrace();
            // get stacktrace as string https://stackoverflow.com/a/1149712
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            return new ResponseEntity<>("Error while loading domlx into string: \n" + e.getMessage() + "\nStackTrace:\n" + sw, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        logger.info("domlx loaded into string");
        String domlx = outputStream.toString();

        // return domlx
        return new ResponseEntity<>(domlx, HttpStatus.CREATED);
    }

    public static String getUniqueFileName(String directory, String extension) {
        String fileName = MessageFormat.format("{0}.{1}", UUID.randomUUID(), extension.trim());
        return Paths.get(directory, fileName).toString();
    }
}
