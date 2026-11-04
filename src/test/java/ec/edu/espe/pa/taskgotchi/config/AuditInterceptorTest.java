package ec.edu.espe.pa.taskgotchi.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AuditInterceptorTest {

    private final AuditInterceptor interceptor = new AuditInterceptor();

    static class TaskController {
        public void completeTask() { }
        public void createTask() { }
    }

    private HandlerMethod handler(String name) throws NoSuchMethodException {
        return new HandlerMethod(new TaskController(), TaskController.class.getMethod(name));
    }

    @Test
    void auditsSuccessfulMutationWithResourceId() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("PATCH", "/api/v1/tasks/7/complete");
        request.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, Map.of("id", "7"));

        assertThat(interceptor.describe(request, 200, null, handler("completeTask"), null)).hasValue(
                "action=Task.completeTask method=PATCH path=/api/v1/tasks/7/complete ids={id=7} status=200 result=OK");
    }

    @Test
    void auditsCreatedResourceIdFromLocationHeader() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/tasks");

        assertThat(interceptor.describe(request, 201, "http://localhost/api/v1/tasks/42", handler("createTask"), null))
                .hasValueSatisfying(line -> assertThat(line).contains("ids={createdId=42}", "status=201", "result=OK"));
    }

    @Test
    void marksRejectedMutationAsFailed() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("PATCH", "/api/v1/tasks/7/complete");

        assertThat(interceptor.describe(request, 422, null, handler("completeTask"), null)).hasValueSatisfying(line ->
                assertThat(line).contains("status=422", "result=FAILED"));
    }

    @Test
    void ignoresReadOnlyRequests() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/tasks");

        assertThat(interceptor.describe(request, 200, null, handler("completeTask"), null)).isEmpty();
    }
}
