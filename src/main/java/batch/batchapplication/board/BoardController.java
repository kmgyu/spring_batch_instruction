package batch.batchapplication.board;

import batch.batchapplication.board.dto.BoardResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/board")
@RequiredArgsConstructor
public class BoardController {
  private final BoardService boardService;

  @GetMapping("/{id}")
  public ResponseEntity<?> getContent(@PathVariable Long id) {
    BoardResponseDTO boardResponseDTO = boardService.findById(id);

    if (boardResponseDTO == null) {
      // for failure request
      return ResponseEntity.notFound().build();
    }

    return ResponseEntity.ok(boardResponseDTO);
  }
}
