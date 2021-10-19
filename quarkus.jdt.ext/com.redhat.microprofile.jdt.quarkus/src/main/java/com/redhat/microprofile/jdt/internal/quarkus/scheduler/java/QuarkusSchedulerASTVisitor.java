/*******************************************************************************
* Copyright (c) 2021 Red Hat Inc. and others.
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License v. 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
* which is available at https://www.apache.org/licenses/LICENSE-2.0.
*
* SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.jdt.internal.quarkus.scheduler.java;

import static org.eclipse.lsp4mp.jdt.core.utils.AnnotationUtils.getAnnotationMemberValueExpression;
import static org.eclipse.lsp4mp.jdt.core.utils.AnnotationUtils.isMatchAnnotation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4mp.jdt.core.java.diagnostics.JavaDiagnosticsContext;
import org.eclipse.lsp4mp.jdt.core.java.validators.JavaASTValidator;
import org.eclipse.lsp4mp.jdt.core.utils.JDTTypeUtils;

import com.redhat.microprofile.jdt.internal.quarkus.QuarkusConstants;
import com.redhat.microprofile.jdt.internal.quarkus.scheduler.SchedulerErrorCodes;
import com.redhat.microprofile.jdt.internal.quarkus.scheduler.SchedulerUtils;

public class QuarkusSchedulerASTVisitor extends JavaASTValidator {

	private final Map<TypeDeclaration, Set<String>> methodsCache;

	private static Logger LOGGER = Logger.getLogger(QuarkusSchedulerASTVisitor.class.getName());

	public QuarkusSchedulerASTVisitor() {
		super();
		this.methodsCache = new HashMap<>();
	}

	@Override
	public boolean isAdaptedForDiagnostics(JavaDiagnosticsContext context, IProgressMonitor monitor)
			throws CoreException {
		IJavaProject javaProject = context.getJavaProject();
		return JDTTypeUtils.findType(javaProject, QuarkusConstants.SCHEDULED_ANNOTATION) != null;
	}

	@Override
	public boolean visit(MethodDeclaration node) {
		try {
			List modifiers = node.modifiers();
			for (Object modifier : modifiers) {
				if (modifier instanceof Annotation) {
					Annotation annotation = (Annotation) modifier;
					if (isMatchAnnotation(annotation, QuarkusConstants.SCHEDULED_ANNOTATION)) {
						validateScheduledAnnotation(node, (NormalAnnotation) annotation);
					}
				}
			}
		} catch (JavaModelException e) {
			LOGGER.log(Level.WARNING, "An exception occurred when attempting to validate the annotation marked method");
		}
		super.visit(node);
		return true;
	}

	/**
	 * Checks if the given method declaration has a @Scheduled annotation, and if
	 * so, provides diagnostics it's member(s)
	 *
	 * @param node       The method declaration to validate
	 * @param annotation The @Scheduled annotation
	 * @throws JavaModelException
	 */
	private void validateScheduledAnnotation(MethodDeclaration node, NormalAnnotation annotation)
			throws JavaModelException {
		Expression cronExpr = getAnnotationMemberValueExpression(annotation,
				QuarkusConstants.SCHEDULED_ANNOTATION_CRON);
		if (cronExpr != null) {
			SchedulerErrorCodes cronPartFault = SchedulerUtils.validateCronPattern(cronExpr.toString());
			if (cronPartFault != null) {
				super.addDiagnostic(cronPartFault.getErrorMessage(), QuarkusConstants.QUARKUS_PREFIX, cronExpr,
						cronPartFault, DiagnosticSeverity.Warning);
			}
		}
		Expression everyExpr = getAnnotationMemberValueExpression(annotation,
				QuarkusConstants.SCHEDULED_ANNOTATION_EVERY);
		if (everyExpr != null) {
			SchedulerErrorCodes everyMemberFault = SchedulerUtils.validateDurationParse(everyExpr.toString());
			if (everyMemberFault != null) {
				super.addDiagnostic(everyMemberFault.getErrorMessage(), QuarkusConstants.QUARKUS_PREFIX, everyExpr,
						everyMemberFault, DiagnosticSeverity.Warning);
			}
		}
		Expression delayedExpr = getAnnotationMemberValueExpression(annotation,
				QuarkusConstants.SCHEDULED_ANNOTATION_DELAYED);
		if (delayedExpr != null) {
			SchedulerErrorCodes delayedMemberFault = SchedulerUtils.validateDurationParse(delayedExpr.toString());
			if (delayedMemberFault != null) {
				super.addDiagnostic(delayedMemberFault.getErrorMessage(), QuarkusConstants.QUARKUS_PREFIX, delayedExpr,
						delayedMemberFault, DiagnosticSeverity.Warning);
			}
		}
	}

}