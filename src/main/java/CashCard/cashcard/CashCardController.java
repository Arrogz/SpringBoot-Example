package CashCard.cashcard;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.*;
import java.security.Principal;
import org.springframework.web.util.UriComponentsBuilder;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/cashcards")
class CashCardController {

    private final CashCardRepository cashCardRepository;

    private CashCardController(CashCardRepository cashCardRepository) {
        this.cashCardRepository = cashCardRepository;
    }

    @GetMapping("/{requestedId}")
    private ResponseEntity<CashCard> findById(@PathVariable Long requestedId, Principal principal) {
         CashCard cashCard = findCashCard(requestedId, principal);
        if (cashCard != null) {
            return ResponseEntity.ok(cashCard);
        } else {
            return ResponseEntity.notFound().build();
        }
 }

   @GetMapping
   private ResponseEntity<Iterable<CashCard>> findAll(Pageable pageable, Principal principal) {
        Page<CashCard> page = cashCardRepository.findByOwner(
            principal.getName(),
            PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), pageable.getSortOr(Sort.by("amount").ascending()))
        );

        return ResponseEntity.ok(page.getContent());
   }


   @PostMapping
    private ResponseEntity<Void> createCashCard(@RequestBody CashCard newCashCard, UriComponentsBuilder ucb, Principal principal) {
            CashCard cashCardWithOwner = new CashCard(newCashCard.id(), newCashCard.amount(), principal.getName());
            CashCard savedCashCard = cashCardRepository.save(cashCardWithOwner);
            URI locationOfNewCashCard = UriComponentsBuilder.fromPath("/cashcards/{id}").buildAndExpand(savedCashCard.id()).toUri();
            return ResponseEntity.created(locationOfNewCashCard).build();
    }

    @PutMapping("/{requestedId}")
    private ResponseEntity<Void> putCashCard(@PathVariable Long requestedId, @RequestBody CashCard cashCardUpdate, Principal principal){
        CashCard cashCard = findCashCard(requestedId, principal);
        if (cashCard == null) {
            return ResponseEntity.notFound().build();
        }
        CashCard updatedCashCard = new CashCard(cashCard.id(), cashCardUpdate.amount(), cashCard.owner());
        cashCardRepository.save(updatedCashCard);
        return ResponseEntity.noContent().build();
    }

    private CashCard findCashCard(Long requestedId, Principal principal) {
        return cashCardRepository.findByIdAndOwner(requestedId, principal.getName());
    }   
}