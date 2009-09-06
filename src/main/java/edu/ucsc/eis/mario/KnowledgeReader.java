package edu.ucsc.eis.mario;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderError;
import org.drools.builder.KnowledgeBuilderErrors;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.io.ResourceFactory;

import com.google.common.base.Preconditions;

public class KnowledgeReader {
	public static KnowledgeBase getKnowledgeBase(String ruleResource) {
		return getKnowledgeBase(ruleResource, null);
	}
	
	public static KnowledgeBase getKnowledgeBase(String ruleResource, 
			String ruleFlowResource) 
		throws IllegalArgumentException {
		Preconditions.checkNotNull(ruleResource, "Must pass a resource");
		KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
		kbuilder.add(ResourceFactory.newClassPathResource(ruleResource), 
				ResourceType.DRL);
		
		if (ruleFlowResource != null) {
			kbuilder.add(ResourceFactory.newClassPathResource(ruleFlowResource), 
					ResourceType.DRF);
		}
		
		KnowledgeBuilderErrors errors = kbuilder.getErrors();
		
		if (errors.size() > 0) {
			for (KnowledgeBuilderError error: errors) {
				System.err.println(error);
			}
			throw new IllegalArgumentException("Could not parse knowledge.");
		}
		
		KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
		kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());
		return kbase;
	}
}
