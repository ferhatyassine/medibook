package com.medibook.service;

import jakarta.ejb.ApplicationException;

/**
 * Exception levée quand une règle métier n'est pas respectée
 * (créneau déjà pris, email déjà utilisé, suppression impossible...).
 * <p>
 * <b>@ApplicationException(rollback = true)</b> :
 * <ul>
 *   <li>le conteneur EJB la transmet telle quelle à l'appelant
 *       (sans l'envelopper dans une {@code EJBException}) ;</li>
 *   <li>la transaction en cours est annulée (rollback) : aucune modification
 *       partielle n'est enregistrée en base.</li>
 * </ul>
 * Son message est destiné à être affiché à l'utilisateur.
 */
@ApplicationException(rollback = true)
public class MetierException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public MetierException(String message) {
        super(message);
    }
}
