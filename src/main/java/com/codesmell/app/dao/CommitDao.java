package com.codesmell.app.dao;

import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import com.codesmell.app.model.Commit;



public interface CommitDao extends MongoRepository<Commit, String>
{
	
	
}
