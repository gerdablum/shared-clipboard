package de.alina.clipboard;

import de.alina.clipboard.exception.WebSocketException;
import de.alina.clipboard.exception.PersistenceException;
import de.alina.clipboard.exception.UnauthorizedException;
import de.alina.clipboard.model.DataType;
import de.alina.clipboard.model.User;
import de.alina.clipboard.repo.DataManager;
import de.alina.clipboard.repo.IDataManager;
import net.glxn.qrgen.core.image.ImageType;
import net.glxn.qrgen.javase.QRCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.HtmlUtils;

import java.io.*;
import java.net.URL;
import java.util.UUID;


@RestController
public class ClipboardRestController {

    private Logger logger = LoggerFactory.getLogger(ClipboardRestController.class);
    private final IDataManager database;

    @Autowired
    public ClipboardRestController(DataManager database) {
        super();
        this.database = database;
    }

    @Autowired
    private SimpMessagingTemplate msgTemplate;

    /**
     * This call is made from the smartphone to let the server know that it successfully saved the users id.
     * The server then triggers a web socket call to let the client know the authorization is complete.
     * @param id the id scanned with the smartphone
     * @return
     */
    @RequestMapping(value = "/acknowledge")
    public String acknowledgeId(@RequestParam(value = "id") String id) {
        id = HtmlUtils.htmlEscape(id);
        if (isInputInvalid(id)) {
            throw new UnauthorizedException();
        }
        try {
            msgTemplate.convertAndSend("/topic/acknowledge/" + id, "acknowledged");
        } catch (Exception e) {
            e.printStackTrace();
            throw new WebSocketException();
        }

        return "successful";
    }

    /**
     * returns an QR image which contains an id for user authentication. The user scans
     * the QR code with their smartphone. So client and server share the same id.
     * @param id user id
     * @return qr image
     */
    @RequestMapping(value = "/qr-image.png", produces = MediaType.IMAGE_PNG_VALUE)
    public byte[] getQRImage(@RequestParam(value = "id") String id) {
        id = HtmlUtils.htmlEscape(id);
        if (isInputInvalid(id)) {
            throw new UnauthorizedException();
        }
        ByteArrayOutputStream stream = QRCode.from(id).to(ImageType.PNG).stream();
        byte[] rawImage = stream.toByteArray();
        try {
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return rawImage;
    }

    /**
     * Is called on every page load if no id is stored in cookies. Creates a new user id with 10 minutes
     * validation
     * @return User object which contains id
     */
    @RequestMapping(value = "/get-id")
    public User getID() {
        User u = new User(UUID.randomUUID());
        database.createUser(u.id);
        logger.debug("created user with id: " + u.id.toString());
        return u;
    }

    /**
     * receives string data the user sends to the server and stores it for 10 minutes.
     * Use this method only to send plain short texts like links ect.
     * @param id the users id
     * @param data which was sent to the server
     * @return
     */
    @RequestMapping(value = "/send-data", method = RequestMethod.POST)
    public String sendData(@RequestHeader(value = "id") String id,
                           @RequestHeader(value = "data") String data) {
        id = HtmlUtils.htmlEscape(id);
        if (isInputInvalid(id)) {
            throw new UnauthorizedException();
        }
        User user = new User();
        user.id = UUID.fromString(id);
        user.stringData = HtmlUtils.htmlEscape(data);
        user.type = DataType.STRING;
        try {
            database.saveData(user);
        } catch (Exception e) {
            e.printStackTrace();
            // TODO add this to every database call
            throw new PersistenceException();
        }
        msgTemplate.convertAndSend("/topic/data-received/" + user.id, data);
        return "successful";
    }

    /**
     * Upload larger data like images or other files
     * @param file the file to upload
     * @return successfull if the file was uploaded or an error message
     */
    @RequestMapping(value = "/upload-data", method = RequestMethod.POST)
    public String uploadFileData(@RequestParam(value = "file") MultipartFile file,
                                 @RequestParam(value = "id") String id) {
        if (isInputInvalid(id)) {
            throw new UnauthorizedException();
        }
        try {
            database.storeFile(file, UUID.fromString(id));
        } catch (Exception e) {
            e.printStackTrace();
            throw new PersistenceException();
        }
        return "successful";
    }

    /**
     * gets the stored data for a specific user
     * @param id unique user id
     * @return stored data or unauthorized exception
     */
    @RequestMapping(value = "/get-data")
    public User getData(@RequestParam(value = "id") String id) {
        id = HtmlUtils.htmlEscape(id);
        if (isInputInvalid(id)) {
            throw new UnauthorizedException();
        }
        UUID uuid = UUID.fromString(id);
        User user = database.getData(uuid);
        if (user.type == DataType.FILE || user.type == DataType.IMAGE_FILE) {
            // TODO get stored file
            user.fileUrl = "http://www.thisisanurlinthefuture.de";
        }
        return user;
    }

    private boolean isInputInvalid(String id) {
        UUID uuid;
        try {
            uuid = UUID.fromString(id);
        } catch (Exception e) {
            return true;
        }
        return !database.isUserExisting(uuid);
    }
}
