package no.nav.amt_enhetsregister.service

import org.springframework.transaction.TransactionStatus

class DummyTransactionStatus : TransactionStatus {
	override fun hasSavepoint(): Boolean {
		return false
	}

	override fun isNewTransaction(): Boolean {
		return false
	}

	override fun setRollbackOnly() {
		// Do nothing in the stub implementation
	}

	override fun isRollbackOnly(): Boolean {
		return false
	}

	override fun isCompleted(): Boolean {
		return true
	}

	override fun createSavepoint(): Any {
		throw UnsupportedOperationException("Savepoints not supported in DummyTransactionStatus")
	}

	override fun rollbackToSavepoint(savepoint: Any) {
		throw UnsupportedOperationException("Savepoints not supported in DummyTransactionStatus")
	}

	override fun releaseSavepoint(savepoint: Any) {
		throw UnsupportedOperationException("Savepoints not supported in DummyTransactionStatus")
	}

	override fun flush() {
		// Do nothing in the stub implementation
	}
}

