package tk.tcomad.testsystem.endpoint;

import java.util.List;
import java.util.stream.Collectors;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import tk.tcomad.testsystem.exception.NotFoundException;
import tk.tcomad.testsystem.model.api.QuestionApi;
import tk.tcomad.testsystem.model.mapper.QuestionMapper;
import tk.tcomad.testsystem.model.persistence.Question;
import tk.tcomad.testsystem.repository.QuestionRepository;
import tk.tcomad.testsystem.repository.TestRepository;

@RestController
@Secured({"ROLE_ADMIN", "ROLE_TEACHER"})
@RequestMapping("tests/{testId}/questions")
@RequiredArgsConstructor
public class QuestionEndpoint {

    @NonNull
    private final QuestionRepository questionRepository;
    @NonNull
    private final TestRepository testRepository;
    @NonNull
    private final QuestionMapper questionMapper;

    @GetMapping
    public List<QuestionApi> getTestQuestions(@PathVariable String testId) {
        return questionRepository.findAllByTestId(testId).stream()
                                 .map(questionMapper::toQuestionApi)
                                 .collect(Collectors.toList());
    }

    @GetMapping("/{questionId}")
    public QuestionApi getQuestion(@PathVariable String testId, @PathVariable Long questionId) {
        return questionRepository.findByTestIdAndId(testId, questionId)
                                 .map(questionMapper::toQuestionApi)
                                 .orElseThrow(() -> new NotFoundException("Question not found"));
    }

    @PostMapping
    public QuestionApi saveQuestion(@PathVariable String testId, @RequestBody QuestionApi question) {
        if (!testRepository.existsById(testId)) {
            throw new NotFoundException("Test not found");
        }

        question.setTestId(testId);

        Question toSave = questionMapper.toQuestionDb(question);
        Question saved = questionRepository.save(toSave);

        return questionMapper.toQuestionApi(saved);
    }

    @DeleteMapping("/{questionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteQuestion(@PathVariable String testId, @PathVariable Long questionId) {
        questionRepository.deleteByTestIdAndId(testId, questionId);
    }
}
