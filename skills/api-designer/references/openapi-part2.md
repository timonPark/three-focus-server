<!-- part 2/6 of openapi.md -->

## Paths and Operations

### Complete Endpoint Example

```yaml
paths:
  /users:
    get:
      summary: List users
      description: Retrieve a paginated list of users with optional filtering
      operationId: listUsers
      tags:
        - Users

      parameters:
        - name: offset
          in: query
          description: Number of items to skip
          required: false
          schema:
            type: integer
            minimum: 0
            default: 0

        - name: limit
          in: query
          description: Maximum number of items to return
          required: false
          schema:
            type: integer
            minimum: 1
            maximum: 100
            default: 20

        - name: status
          in: query
          description: Filter by user status
          required: false
          schema:
            type: string
            enum:
              - active
              - inactive
              - suspended

      security:
        - bearerAuth: []

      responses:
        '200':
          description: Successful response
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/UserListResponse'
              examples:
                success:
                  $ref: '#/components/examples/UserListSuccess'

        '401':
          $ref: '#/components/responses/Unauthorized'

        '429':
          $ref: '#/components/responses/RateLimitExceeded'

    post:
      summary: Create user
      description: Create a new user account
      operationId: createUser
      tags:
        - Users

      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/CreateUserRequest'
            examples:
              basic:
                $ref: '#/components/examples/CreateUserBasic'

      responses:
        '201':
          description: User created successfully
          headers:
            Location:
              description: URL of the created user
              schema:
                type: string
                format: uri
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/User'

        '400':
          $ref: '#/components/responses/ValidationError'

        '409':
          description: User already exists
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/Error'

  /users/{userId}:
    parameters:
      - name: userId
        in: path
        description: User ID
        required: true
        schema:
          type: integer
          format: int64

    get:
      summary: Get user
      description: Retrieve a specific user by ID
      operationId: getUser
      tags:
        - Users

      responses:
        '200':
          description: Successful response
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/User'

        '404':
          $ref: '#/components/responses/NotFound'

    put:
      summary: Update user
      description: Replace user data
      operationId: updateUser
      tags:
        - Users

      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/UpdateUserRequest'

      responses:
        '200':
          description: User updated successfully
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/User'

        '404':
          $ref: '#/components/responses/NotFound'

    delete:
      summary: Delete user
      description: Delete a user account
      operationId: deleteUser
      tags:
        - Users

      responses:
        '204':
          description: User deleted successfully

        '404':
          $ref: '#/components/responses/NotFound'
```

