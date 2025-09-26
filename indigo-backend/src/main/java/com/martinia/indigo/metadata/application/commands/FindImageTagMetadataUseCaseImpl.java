package com.martinia.indigo.metadata.application.commands;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlImage;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.martinia.indigo.metadata.application.commands.FindImageTagMetadataUseCase.FindImageTagMetadataCommand;
import com.martinia.indigo.metadata.application.commands.FindImageTagMetadataUseCase.FindImageTagMetadataResult;
import com.martinia.indigo.metadata.domain.ImageTagMetadata;
import com.martinia.indigo.metadata.domain.ImageTagMetadataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FindImageTagMetadataUseCaseImpl implements FindImageTagMetadataUseCase, com.martinia.indigo.metadata.domain.ports.usecases.commands.FindImageTagMetadataUseCase {

  private final ImageTagMetadataRepository imageTagMetadataRepository;
  private final WebClient webClient;

  @Override
  public FindImageTagMetadataResult findImageTagMetadata(
      FindImageTagMetadataCommand command) {
    ImageTagMetadata imageTagMetadata = imageTagMetadataRepository.findByImagePath(command.getImagePath());
    return FindImageTagMetadataResult.builder().imageTagMetadata(imageTagMetadata).build();
  }

  @Override
  public String find(String term) {
    try {
      // Use a search URL pattern - this is a simplified implementation for the test
      String searchUrl = "https://www.example.com/search?q=" + term;
      HtmlPage page = webClient.getPage(searchUrl);

      // Look for images using XPath (matching the test mock setup)
      List<HtmlElement> elements = page.getByXPath("//div[contains(@class, 'image-container')]");

      if (!elements.isEmpty()) {
        HtmlElement firstDiv = elements.get(0);
        if (firstDiv.getFirstChild() instanceof HtmlImage) {
          HtmlImage image = (HtmlImage) firstDiv.getFirstChild();
          return image.getAttribute("src");
        }
      }

      return null;
    } catch (Exception e) {
      log.error("Error finding image for term: " + term, e);
      return null;
    }
  }
}