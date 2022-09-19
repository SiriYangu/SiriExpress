CREATE TABLE MpesaExpress(
	MpesaExpressID BIGINT IDENTITY(1,1) PRIMARY KEY,
	BusinessShortCode VARCHAR(10) NOT NULL,
	Timestamp VARCHAR(100) NULL,
	TransactionType VARCHAR(100) NULL,
	Amount VARCHAR(100) NOT NULL,
	PartyA VARCHAR(100) NOT NULL,
	PartyB VARCHAR(100) NOT NULL,
	PhoneNumber VARCHAR(100) NOT NULL,
	CallBackURL VARCHAR(MAX) NULL,
	AccountReference VARCHAR(100) NULL,
	TransactionDesc VARCHAR(100) NULL,
	ACK_MerchantRequestID VARCHAR(MAX) NULL,
	ACK_CheckoutRequestID VARCHAR(MAX) NULL,
	ACK_ResponseDescription VARCHAR(MAX) NULL,
	ACK_ResponseCode VARCHAR(MAX) NULL,
	ACK_CustomerMessage VARCHAR(MAX) NULL,
	R_MerchantRequestID VARCHAR(MAX) NULL,
	R_CheckoutRequestID VARCHAR(MAX) NULL,
	R_ResultCode VARCHAR(MAX) NULL,
	R_ResultDesc VARCHAR(MAX) NULL,
	R_MpesaReceiptNumber VARCHAR(MAX) NULL,
	R_TransactionDate VARCHAR(MAX) NULL,
	R_PhoneNumber VARCHAR(MAX) NULL
);
GO


----------------------------------------------------

CREATE PROCEDURE dbo.CreateSTKExpressRequest  
	@BusinessShortCode varchar(100), 
	@Timestamp varchar(100), 
	@TransactionType varchar(100), 
	@Amount varchar(100), 
	@PartyA varchar(100),
	@PartyB varchar(100),
	@PhoneNumber varchar(100),
	@AccountReference varchar(100),
	@TransactionDesc varchar(100),
	@ACK_MerchantRequestID varchar(100),
	@ACK_CheckoutRequestID varchar(100),
	@ACK_ResponseDescription varchar(100),
	@ACK_ResponseCode varchar(100),
	@ACK_CustomerMessage varchar(100)

	AS
	SET NOCOUNT ON

	INSERT INTO [dbo].[MpesaExpress]
			   ( [BusinessShortCode]
				,[Timestamp]
				,[TransactionType]
				,[Amount]
				,[PartyA]
				,[PartyB]
				,[PhoneNumber]
				,[AccountReference]
				,[TransactionDesc]
				,[ACK_MerchantRequestID]
				,[ACK_CheckoutRequestID]
				,[ACK_ResponseDescription]
				,[ACK_ResponseCode]
				,[ACK_CustomerMessage])
		 VALUES
			   (@BusinessShortCode, 
				@Timestamp, 
				@TransactionType, 
				@Amount, 
				@PartyA,
				@PartyB,
				@PhoneNumber,
				@AccountReference,
				@TransactionDesc,
				@ACK_MerchantRequestID,
				@ACK_CheckoutRequestID,
				@ACK_ResponseDescription,
				@ACK_ResponseCode,
				@ACK_CustomerMessage)
GO

----------------

CREATE PROCEDURE dbo.UpdateSTKCallBack  
	@R_MerchantRequestID varchar(100), 
	@R_CheckoutRequestID varchar(100), 
	@R_ResultCode varchar(100), 
	@R_ResultDesc varchar(100), 
	@R_MpesaReceiptNumber varchar(100),
	@R_TransactionDate varchar(100),
	@R_PhoneNumber varchar(100)
	
	AS
	SET NOCOUNT ON

	UPDATE [MpesaExpress]
	SET
	[R_MerchantRequestID] = @R_MerchantRequestID, 
	[R_CheckoutRequestID]=@R_CheckoutRequestID, 
	[R_ResultCode]=@R_ResultCode, 
	[R_ResultDesc]=@R_ResultDesc, 
	[R_MpesaReceiptNumber]=@R_MpesaReceiptNumber,
	[R_TransactionDate]=@R_TransactionDate,
	[R_PhoneNumber]=@R_PhoneNumber

	WHERE [ACK_MerchantRequestID] = @R_MerchantRequestID

GO

