public class TestMath {
    public static void main(String[] args) {
        float hullYaw = 0.0f; // 0 yaw = South
        float pitch = 0.0f;
        float currentSpeed = 1.0f;

        float radYaw = (float) Math.toRadians(hullYaw);
        float radPitch = (float) Math.toRadians(pitch);
        double fX = -Math.sin(radYaw) * Math.cos(radPitch);
        double fY = -Math.sin(radPitch);
        double fZ = Math.cos(radYaw) * Math.cos(radPitch);

        double targetVelX = fX * currentSpeed;
        double targetVelY = fY * currentSpeed;
        double targetVelZ = fZ * currentSpeed;

        System.out.println("BEFORE FIX:");
        System.out.printf("DEBUG SbwPhysics: Entity=a_10a yRot=%.2f deltaX=%.4f deltaZ=%.4f currentSpeed=%.4f%n",
                          hullYaw, targetVelX, targetVelZ, currentSpeed);
        System.out.println();
        System.out.println("Analysis:");
        System.out.println("- At yRot=0 (South), deltaZ is positive (+1.0).");
        System.out.println("- In Minecraft, +Z is exactly South.");
        System.out.println("- Therefore, the entity is physically moving FORWARD (South) relative to its yaw (South).");
        System.out.println();
        System.out.println("Why did previous 180-degree visual fixes 'fail to visually change anything'?");
        System.out.println("Because SbwNpcRenderer.applyRotations is NEVER called by Custom NPCs if the entity is rendered as a model for an EntityCustomNpc! Custom NPCs delegates to RenderCustomNpc which uses its own applyRotations, ignoring the one in SbwNpcRenderer. If this is a standalone entity, Geckolib's root bone animations can override poseStack rotations.");
    }
}
