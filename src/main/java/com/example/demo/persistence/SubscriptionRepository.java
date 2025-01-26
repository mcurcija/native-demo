package com.example.demo.persistence;

import java.util.UUID;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.model.Subscription;

@Repository
public interface SubscriptionRepository extends MongoRepository<Subscription, UUID> {

}