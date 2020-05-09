import com.google.gson.Gson;
import de.alina.clipboard.Application;
import de.alina.clipboard.model.DataType;
import de.alina.clipboard.model.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;


import javax.servlet.http.Cookie;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(
        classes = Application.class)
public class TestClipboardRestController {

    @Autowired
    private MockMvc mvc;

    @Test
    public void testGetID() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/get-id")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    public void testGetQRcodeInvalid() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/qr-image.png")
                .param("id", "test")
                .accept(MediaType.IMAGE_PNG_VALUE))
                .andExpect(status().is(401));
    }

    @Test
    public void testQRcodeValid() throws Exception {
        String userid = getValidUUID();
        mvc.perform(MockMvcRequestBuilders.get("/qr-image.png")
                .param("id", userid)
                .accept(MediaType.IMAGE_PNG_VALUE))
                .andExpect(status().isOk());
    }

    @Test
    public void testSendAndGetData() throws Exception {
        String userid = getValidUUID();
        String testdata = "hello world!";
        MvcResult request = mvc.perform(MockMvcRequestBuilders.post("/send-data")
                .cookie(new Cookie("clipboard.id", userid))
                .header("data", testdata))
                .andExpect(status().isOk())
                .andReturn();
        System.out.println(request);
        MvcResult result = mvc.perform(MockMvcRequestBuilders.get("/get-data")
                .cookie(new Cookie("clipboard.id", userid)))
                .andExpect(status().isOk())
                .andReturn();
        String contentBody = result.getResponse().getContentAsString();
        Gson gson = new Gson();
        User user = gson.fromJson(contentBody, User.class);
        DataType type = user.type;
        assertEquals(testdata, user.stringData);
        assertEquals(DataType.STRING, type);
    }

    @Test
    public void testUploadDownloadFileData() throws Exception {
        String uploadID = getValidUUID();
        MockMultipartFile textFile = new MockMultipartFile(
                "file", "text.txt", "text/plain", "Hello World".getBytes());
        mvc.perform(MockMvcRequestBuilders.multipart("/upload-data")
                .file(textFile).cookie(new Cookie("clipboard.id", uploadID)))
                .andExpect(status().isOk());

        MvcResult result = mvc.perform(MockMvcRequestBuilders.get("/get-data")
                .cookie(new Cookie("clipboard.id", uploadID)))
                .andExpect(status().isOk())
                .andReturn();
        String contentBody = result.getResponse().getContentAsString();
    }

    @Test
    public void testAcknowledgeIdUnauthorized() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/acknowledge")
                .cookie(new Cookie("clipboard.id", "test")))
                .andExpect(status().is(401));
    }

    @Test
    public void testAcknowledgeId() throws Exception {
        String userId = getValidUUID();
        mvc.perform(MockMvcRequestBuilders.get("/acknowledge")
                .cookie(new Cookie("clipboard.id", userId)))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    public void testLogout() throws Exception {
        String userId = getValidUUID();
        mvc.perform(MockMvcRequestBuilders.get("/logout")
                .cookie(new Cookie("clipboard.id", userId)))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    public void testConnectionIsConnected() throws Exception {
        String userId = getValidUUID();
        MvcResult result = mvc.perform(MockMvcRequestBuilders.get("/connected")
                .cookie(new Cookie("clipboard.id", userId)))
                .andExpect(status().isOk())
                .andReturn();
        String contentBody = result.getResponse().getContentAsString();
        assertEquals("true", contentBody);
    }

    @Test
    public void testConnectionIsNotConnected() throws Exception {
        MvcResult result = mvc.perform(MockMvcRequestBuilders.get("/connected")
                .cookie(new Cookie("clipboard.id", UUID.randomUUID().toString())))
                .andExpect(status().isOk())
                .andReturn();
        String contentBody = result.getResponse().getContentAsString();
        assertEquals("false", contentBody);
    }

    private String getValidUUID() throws Exception {
        MvcResult result = mvc.perform(MockMvcRequestBuilders.get("/get-id")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        String contentString = result.getResponse().getContentAsString();
        Gson gson = new Gson();
        User user = gson.fromJson(contentString, User.class);
        return user.id.toString();
    }
}
