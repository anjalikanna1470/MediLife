import os
from typing import List, Optional

from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from dotenv import load_dotenv
from groq import Groq


# ============================================================
# ENVIRONMENT
# ============================================================

load_dotenv()

GROQ_API_KEY = os.getenv("GROQ_API_KEY")
AI_MODEL = os.getenv("AI_MODEL", "openai/gpt-oss-120b")

# Create Groq client only when API key is available.
groq_client = Groq(api_key=GROQ_API_KEY) if GROQ_API_KEY else None


# ============================================================
# FASTAPI APP
# ============================================================

app = FastAPI(
    title="MediLife AI Health Backend",
    description=(
        "Grounded AI Health Copilot API supporting "
        "Patient and Doctor Clinical workflows."
    ),
    version="1.0.0"
)


# ============================================================
# PYDANTIC SCHEMAS
# ============================================================

class PatientContext(BaseModel):
    uid: str
    conditions: List[str] = []
    medications: List[str] = []
    allergies: List[str] = []
    recent_records: List[str] = []


class ChatRequest(BaseModel):
    user_id: str
    question: str
    patient_context: Optional[PatientContext] = None


class ClinicalSummaryRequest(BaseModel):
    doctor_id: str
    patient_id: str
    requested_scopes: List[str]


class AttachmentMetadata(BaseModel):
    name: str = "report.pdf"
    type: str = "DOCUMENT"
    mime_type: Optional[str] = None
    size_bytes: Optional[int] = None


class CareMateChatRequest(BaseModel):
    user_id: str
    question: str = ""
    chat_id: Optional[str] = None
    attachment_metadata: Optional[AttachmentMetadata] = None
    patient_context: Optional[PatientContext] = None


class AIHealthResponse(BaseModel):
    answer: str
    provenance_label: str = "AI Generated"
    context_used: str
    confidence: str = "High"
    limitations: str = (
        "Informational assistance only. "
        "Consult your clinician for medical diagnosis."
    )


# ============================================================
# HEALTH CHECK
# ============================================================

@app.get("/health")
def health_check():
    return {
        "status": "healthy",
        "service": "MediLife Python Backend",
        "ai_model": AI_MODEL,
        "groq_configured": bool(GROQ_API_KEY)
    }


# ============================================================
# HELPER FUNCTIONS
# ============================================================

def require_groq():
    """
    Make sure the Groq API key exists before trying to call Groq.
    """
    if not GROQ_API_KEY or groq_client is None:
        raise HTTPException(
            status_code=500,
            detail=(
                "GROQ_API_KEY is not configured. "
                "Please add GROQ_API_KEY to the backend .env file."
            )
        )


def build_patient_context(context: Optional[PatientContext]) -> str:
    """
    Convert patient context into a safe text block
    that can be supplied to the AI model.
    """

    if not context:
        return (
            "No additional patient context was supplied. "
            "Do not invent patient-specific medical information."
        )

    conditions = (
        ", ".join(context.conditions)
        if context.conditions
        else "None provided"
    )

    medications = (
        ", ".join(context.medications)
        if context.medications
        else "None provided"
    )

    allergies = (
        ", ".join(context.allergies)
        if context.allergies
        else "None provided"
    )

    recent_records = (
        ", ".join(context.recent_records)
        if context.recent_records
        else "None provided"
    )

    return f"""
Patient UID: {context.uid}

Known Conditions:
{conditions}

Current Medications:
{medications}

Known Allergies:
{allergies}

Recent Records:
{recent_records}
""".strip()


def ask_groq(
    user_question: str,
    context_text: str,
    system_instruction: str
) -> str:
    """
    Send a request to Groq and return the generated answer.
    """

    require_groq()

    try:
        completion = groq_client.chat.completions.create(
            model=AI_MODEL,
            temperature=0.2,
            max_tokens=1000,
            messages=[
                {
                    "role": "system",
                    "content": system_instruction
                },
                {
                    "role": "user",
                    "content": (
                        f"AUTHORIZED PATIENT CONTEXT:\n"
                        f"{context_text}\n\n"
                        f"USER QUESTION:\n"
                        f"{user_question}"
                    )
                }
            ]
        )

        answer = completion.choices[0].message.content

        if not answer:
            raise HTTPException(
                status_code=502,
                detail="Groq returned an empty response."
            )

        return answer.strip()

    except HTTPException:
        raise

    except Exception as e:
        print("Groq API error:", repr(e))

        raise HTTPException(
            status_code=502,
            detail=f"AI service error: {str(e)}"
        )


# ============================================================
# PATIENT CHAT / COPILOT
# ============================================================

@app.post("/chat", response_model=AIHealthResponse)
def handle_patient_chat(request: ChatRequest):

    question = request.question.strip()

    if not question:
        raise HTTPException(
            status_code=400,
            detail="Question cannot be empty."
        )

    context_text = build_patient_context(
        request.patient_context
    )

    system_instruction = """
You are MediLife CareMate, a healthcare information assistant.

Your job is to help patients understand their authorized health
information in simple and clear language.

IMPORTANT RULES:

1. Never invent medical records, medications, allergies, diagnoses,
   lab values, or treatment history.

2. Use only the patient context provided to you.

3. If required information is missing, clearly say that it is not
   available in the provided records.

4. Do not diagnose diseases.

5. Do not prescribe or change medications.

6. Do not recommend changing medication dosage.

7. Explain medical terminology in simple language when appropriate.

8. For potentially urgent or dangerous symptoms, advise the user
   to seek appropriate professional or emergency medical care.

9. Keep answers concise but useful.

10. Make it clear that the response is informational and does not
    replace a clinician.
"""

    answer = ask_groq(
        user_question=question,
        context_text=context_text,
        system_instruction=system_instruction
    )

    return AIHealthResponse(
        answer=answer,
        provenance_label="AI Generated",
        context_used=context_text,
        confidence="AI response grounded in supplied patient context",
        limitations=(
            "MediLife CareMate provides informational assistance only. "
            "It does not diagnose, prescribe, or replace a clinician."
        )
    )


# ============================================================
# DOCTOR CLINICAL SUMMARY
# ============================================================

@app.post(
    "/clinical-summary",
    response_model=AIHealthResponse
)
def handle_clinical_summary(
    request: ClinicalSummaryRequest
):

    require_groq()

    scopes = ", ".join(request.requested_scopes)

    context_text = f"""
Doctor ID: {request.doctor_id}
Patient ID: {request.patient_id}
Authorized scopes: {scopes}
""".strip()

    system_instruction = """
You are MediLife Clinical AI, an assistant for authorized healthcare
professionals.

Create a concise longitudinal clinical summary from the information
provided.

Rules:

1. Do not invent patient information.
2. Clearly distinguish supplied information from inference.
3. Do not make a definitive diagnosis.
4. Do not prescribe treatment.
5. Highlight clinically relevant conditions, medications,
   allergies, trends, and follow-up considerations when supplied.
6. Keep the language professional and clinically useful.
7. This is clinical decision support, not a replacement for
   professional medical judgment.
"""

    question = (
        "Generate a concise longitudinal clinical summary for the "
        "authorized patient. Include relevant findings, medications, "
        "allergies, trends, and clinician action items only when "
        "supported by the available context."
    )

    answer = ask_groq(
        user_question=question,
        context_text=context_text,
        system_instruction=system_instruction
    )

    return AIHealthResponse(
        answer=answer,
        provenance_label="AI Generated Clinical Decision Support",
        context_used=f"Authorized scopes: {scopes}",
        confidence="AI generated from authorized clinical context",
        limitations=(
            "Clinical AI is decision support only. "
            "The treating clinician remains responsible for "
            "clinical interpretation and decisions."
        )
    )


# ============================================================
# CAREMATE CHAT
# ============================================================

@app.post(
    "/caremate/chat",
    response_model=AIHealthResponse
)
def handle_caremate_chat(
    request: CareMateChatRequest
):

    if not request.user_id:
        raise HTTPException(
            status_code=401,
            detail="User authentication required."
        )

    question = (request.question or "").strip()
    attachment = request.attachment_metadata

    if not question and not attachment:
        raise HTTPException(
            status_code=400,
            detail="Question or attachment is required."
        )

    context_text = build_patient_context(
        request.patient_context
    )

    # --------------------------------------------------------
    # Attachment information
    # --------------------------------------------------------

    attachment_text = ""

    if attachment:

        attachment_text = f"""
Uploaded attachment:

Name: {attachment.name}
Type: {attachment.type}
MIME type: {attachment.mime_type or "Unknown"}
Size: {attachment.size_bytes or "Unknown"} bytes
""".strip()

    # --------------------------------------------------------
    # Combine context
    # --------------------------------------------------------

    full_context = context_text

    if attachment_text:
        full_context += "\n\n" + attachment_text

    # --------------------------------------------------------
    # Empty question with attachment
    # --------------------------------------------------------

    if not question:
        question = (
            "Please explain what can be understood from the uploaded "
            "medical material. Do not diagnose or prescribe."
        )

    # --------------------------------------------------------
    # System instruction
    # --------------------------------------------------------

    system_instruction = """
You are MediLife CareMate.

You help users understand their own authorized medical information,
medical reports, medicines, and uploaded health-related material.

IMPORTANT:

1. Never invent information.

2. Never claim that you actually saw or analyzed the contents of
   a file unless the file contents have genuinely been supplied
   to the model.

3. If only attachment metadata is provided, say that the metadata
   was received but the actual file contents are not available.

4. Do not diagnose.

5. Do not prescribe medication.

6. Do not tell users to start, stop, or change medication without
   appropriate clinician involvement.

7. Explain reports and medical terminology in understandable language.

8. If symptoms suggest an emergency, advise appropriate urgent
   medical evaluation.

9. Keep answers concise and useful.

10. Clearly distinguish medical information from medical advice.
"""

    answer = ask_groq(
        user_question=question,
        context_text=full_context,
        system_instruction=system_instruction
    )

    return AIHealthResponse(
        answer=answer,
        provenance_label="AI Generated",
        context_used=full_context,
        confidence="AI response grounded in supplied authorized context",
        limitations=(
            "CareMate does not diagnose, prescribe medication, "
            "or replace a clinician. For urgent symptoms, seek "
            "immediate professional care."
        )
    )


# ============================================================
# RUN SERVER
# ============================================================

if __name__ == "__main__":
    import uvicorn

    uvicorn.run(
        "main:app",
        host="0.0.0.0",
        port=8000,
        reload=True
    )