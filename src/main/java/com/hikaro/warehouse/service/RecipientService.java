package com.hikaro.warehouse.service;

import com.hikaro.warehouse.entity.Recipient;
import com.hikaro.warehouse.exception.ResourceNotFoundException;
import com.hikaro.warehouse.repository.RecipientRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RecipientService {

    private final RecipientRepository recipientRepository;

    public RecipientService(RecipientRepository recipientRepository) {
        this.recipientRepository = recipientRepository;
    }

    @Transactional(readOnly = true)
    public List<Recipient> findAll() {
        return recipientRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Recipient getById(Long id) {
        return recipientRepository.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Recipient with id " + id + " not found"
                        )
                );
    }

    @Transactional
    public Recipient create(Recipient recipient) {
        return recipientRepository.save(recipient);
    }

    @Transactional
    public Recipient update(Long id, Recipient updatedRecipient) {
        Recipient recipient = getById(id);
        recipient.setName(updatedRecipient.getName());
        recipient.setType(updatedRecipient.getType());
        recipient.setContactEmail(updatedRecipient.getContactEmail());
        recipient.setAddress(updatedRecipient.getAddress());
        return recipientRepository.save(recipient);
    }

    @Transactional
    public void delete(Long id) {
        recipientRepository.delete(getById(id));
    }
}
