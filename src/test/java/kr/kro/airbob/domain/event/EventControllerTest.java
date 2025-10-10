package kr.kro.airbob.domain.event;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseBody;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.kro.airbob.domain.common.BaseControllerDocumentationTest;
import kr.kro.airbob.domain.event.common.ApplyResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(EventController.class)
@AutoConfigureRestDocs
public class EventControllerTest extends BaseControllerDocumentationTest {

    @Autowired
    private EventController eventController;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EventService eventService;

    @Override
    protected Object getController() {
        return eventController;
    }

    @Test
    void applyEvent_success() throws Exception {
        // given
        Long eventId = 1L;
        Long memberId = 123L;
        int maxParticipant = 100;

        given(eventService.getEventMaxParticipants(eventId)).willReturn(maxParticipant);
        given(eventService.applyToEvent(eq(eventId), eq(memberId), eq(maxParticipant)))
                .willReturn(ApplyResult.SUCCESS);

        // when
        mockMvc.perform(post("/api/event/{eventId}", eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"memberId\": 123 }"))
                // then
                .andExpect(status().isOk())
                .andDo(document("apply-event-success",
                        pathParameters(
                                parameterWithName("eventId").description("이벤트 ID")
                        ),
                        responseBody()
                ));
    }
}
