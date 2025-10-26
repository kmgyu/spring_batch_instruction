package batch.batchapplication.board;

import batch.batchapplication.board.domain.Board;
import batch.batchapplication.board.domain.BoardRepository;
import batch.batchapplication.board.dto.BoardResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
@Service
public class BoardService {
  private final BoardRepository boardRepository;

  public BoardResponseDTO findById(Long id) {
    Board board = boardRepository.findById(id).orElse(null);

    if  (board == null) {
      return null;
    }

    return BoardResponseDTO.builder()
            .id(board.getId())
            .title(board.getTitle())
            .content(board.getContent())
            .build();
  }

}
