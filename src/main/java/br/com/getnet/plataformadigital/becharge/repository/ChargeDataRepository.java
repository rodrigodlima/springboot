package br.com.getnet.plataformadigital.becharge.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import br.com.getnet.plataformadigital.becharge.domain.ChargeData;

@Repository
public interface ChargeDataRepository extends MongoRepository<ChargeData, String> {
	
}
