package dev.jackdaw1101.neon.modules.automated.ai;

import com.theokanning.openai.OpenAiService;
import com.theokanning.openai.moderation.ModerationRequest;
import com.theokanning.openai.moderation.ModerationResult;
import com.theokanning.openai.moderation.Moderation;

import java.util.*;

public class OpenAIModerationManager {
    private final OpenAiService service;
    private final Set<String> enabledCategories;

    public OpenAIModerationManager(String apiKey, List<String> categories) {
        this.service = new OpenAiService(apiKey);

        this.enabledCategories = new HashSet<>(categories);
    }

    /**
     * Moderates the given message and returns a list of flagged categories.
     *
     * @param message The message to be moderated.
     * @return A list of categories the message violated, or an empty list if no violation.
     */
    public List<String> moderate(String message) {
        try {
            ModerationRequest request = new ModerationRequest();
            request.setInput(message);

            ModerationResult response = service.createModeration(request);

            if (response == null || response.getResults() == null || response.getResults().isEmpty()) {
                return Collections.emptyList();
            }

            Moderation moderation = response.getResults().get(0);
            Map<String, Boolean> categoryMap = (Map<String, Boolean>) moderation.getCategories();

            List<String> flagged = new ArrayList<>();
            for (Map.Entry<String, Boolean> entry : categoryMap.entrySet()) {
                if (entry.getValue() && enabledCategories.contains(entry.getKey().toLowerCase())) {
                    flagged.add(entry.getKey());
                }
            }
            return flagged;

        } catch (retrofit2.HttpException e) {
            System.err.println("OpenAI API call failed: " + e.response().errorBody());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Collections.emptyList();
    }
}
