package models;

import play.data.format.Formats;
import play.data.validation.Constraints;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.util.Date;

/**
 * Computer entity managed by Ebean
 */
@Entity 
public class Person extends BaseModel {

	private static final long serialVersionUID = 1L;

	@Constraints.Required
	public String name;
	
	@Constraints.Required
	public Integer age;
	
	@Constraints.Required
	public String email;
	
	@ManyToOne
	public Sex sex;
	
}

